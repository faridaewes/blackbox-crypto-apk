package com.blackbox.crypto.data

import android.content.Context
import com.blackbox.crypto.BuildConfig
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SessionStore(context: Context) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context, "blackbox_session", MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    var cookie: String? get() = prefs.getString("cookie", null) set(v) { if (v == null) prefs.edit().remove("cookie").apply() else prefs.edit().putString("cookie", v).apply() }
    var baseUrl: String
        get() = prefs.getString("base_url", null) ?: BuildConfig.DEFAULT_API_BASE_URL
        set(v) = prefs.edit().putString("base_url", v.trimEnd('/')).apply()
    fun clear() = prefs.edit().remove("cookie").apply()
}

data class Bot(val id: String, val name: String, val status: String, val mode: String)
data class Signal(val symbol: String, val action: String, val score: Int, val confidence: Int)
data class ExchangeConnection(val id: String, val provider: String, val label: String, val status: String, val marketType: String, val testnet: Boolean)
data class WalletConnection(val id: String, val provider: String, val chain: String, val address: String, val status: String)
data class AppNotification(val id: String, val title: String, val body: String, val priority: String, val createdAt: String)

class BlackboxApi(private val context: Context) {
    private val store = SessionStore(context)
    private val client = OkHttpClient.Builder().cookieJar(object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) { cookies.firstOrNull { it.name == "blackbox_session" }?.let { store.cookie = "${it.name}=${it.value}" } }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = store.cookie?.let { listOf(Cookie.parse(url, it)!!) } ?: emptyList()
    }).connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()

    fun configured() = store.baseUrl.startsWith("https://")
    fun setBaseUrl(url: String) { require(url.startsWith("https://")) { "Production Android hanya mengizinkan HTTPS." }; store.baseUrl = url }
    fun loggedIn() = store.cookie != null

    private fun request(path: String, method: String = "GET", body: JSONObject? = null): JSONObject {
        val base = store.baseUrl.ifBlank { throw IllegalStateException("API URL belum dikonfigurasi") }
        val b = Request.Builder().url(base + path).header("Accept", "application/json")
        if (method != "GET") b.method(method, (body?.toString() ?: "{}").toRequestBody("application/json".toMediaType()))
        val r = client.newCall(b.build()).execute()
        val text = r.body?.string().orEmpty()
        val obj = try { JSONObject(text) } catch (_: Exception) { JSONObject().put("error", text.ifBlank { "HTTP ${r.code}" }) }
        if (!r.isSuccessful) throw ApiException(r.code, obj.optString("error", "Request gagal"))
        return obj
    }

    fun login(email: String, password: String, mfa: String?): JSONObject {
        val body = JSONObject().put("email", email).put("password", password); if (!mfa.isNullOrBlank()) body.put("mfaCode", mfa)
        return request("/api/auth/login", "POST", body)
    }
    fun logout() { runCatching { request("/api/auth/logout", "POST") }; store.clear() }
    fun bots(): Pair<List<Bot>, JSONObject> { val o=request("/api/bots"); val a=o.optJSONArray("bots")?:JSONArray(); return (0 until a.length()).map { val x=a.getJSONObject(it); Bot(x.getString("id"),x.getString("name"),x.getString("status"),x.getString("mode")) } to (o.optJSONObject("entitlements")?:JSONObject().put("label",o.optString("currentTier","DEMO")).put("tierExpiresAt",o.optString("tierExpiresAt",""))) }
    fun signals(): List<Signal> { val o=request("/api/signals"); val a=o.optJSONArray("signals")?:JSONArray(); return (0 until a.length()).map { val x=a.getJSONObject(it); Signal(x.optString("symbol"),x.optString("action"),x.optInt("score"),x.optInt("confidence")) } }
    fun risk(): JSONObject = request("/api/risk/status")
    fun notifications(): List<AppNotification> { val o=request("/api/notifications?limit=10"); val a=o.optJSONArray("notifications")?:JSONArray(); return (0 until a.length()).map { val x=a.getJSONObject(it); AppNotification(x.getString("id"),x.getString("title"),x.getString("body"),x.optString("priority","NORMAL"),x.optString("createdAt","")) } }
    fun control(id: String, action: String, riskAccepted: Boolean = false) = request("/api/bots/$id/control", "POST", JSONObject().put("action", action).put("riskAccepted", riskAccepted))
    fun closeAll(id: String) = request("/api/bots/$id/close-all", "POST")
    fun deleteBot(id: String) = request("/api/bots/$id", "DELETE")
    fun exchangeConnections(): List<ExchangeConnection> { val o=request("/api/connections/exchange"); val a=o.optJSONArray("connections")?:JSONArray(); return (0 until a.length()).map { val x=a.getJSONObject(it); ExchangeConnection(x.getString("id"),x.getString("provider"),x.getString("label"),x.getString("status"),x.optString("marketType","SPOT"),x.optBoolean("testnet",false)) } }
    fun walletConnections(): List<WalletConnection> { val o=request("/api/connections/wallet"); val a=o.optJSONArray("wallets")?:JSONArray(); return (0 until a.length()).map { val x=a.getJSONObject(it); WalletConnection(x.getString("id"),x.getString("provider"),x.getString("chain"),x.getString("address"),x.getString("status")) } }
    fun connectExchange(provider:String,label:String,apiKey:String,secret:String,password:String,uid:String,testnet:Boolean,spot:Boolean,futures:Boolean,mfaCode:String) = requestWithMfa("/api/connections/exchange", "POST", JSONObject().put("provider",provider).put("label",label).put("apiKey",apiKey).put("secret",secret).put("password",password).put("uid",uid).put("testnet",testnet).put("marketType",if(futures)"FUTURES" else "SPOT").put("maxLeverage",1).put("permissions",JSONObject().put("trade",true).put("spot",spot).put("futures",futures).put("withdraw",false)),mfaCode)
    fun disconnectExchange(id: String, mfaCode:String) = requestWithMfa("/api/connections/exchange", "DELETE", JSONObject().put("id", id),mfaCode)
    fun disconnectWallet(id: String, mfaCode:String) = requestWithMfa("/api/connections/wallet", "DELETE", JSONObject().put("id", id),mfaCode)
    fun security(): JSONObject = request("/api/security")
    fun health(): JSONObject = request("/api/health")
    private fun requestWithMfa(path:String,method:String,body:JSONObject,mfaCode:String):JSONObject{val base=store.baseUrl.ifBlank{throw IllegalStateException("API URL belum dikonfigurasi")};val b=Request.Builder().url(base+path).header("Accept","application/json").header("x-blackbox-mfa-code",mfaCode);b.method(method,body.toString().toRequestBody("application/json".toMediaType()));val r=client.newCall(b.build()).execute();val text=r.body?.string().orEmpty();val obj=try{JSONObject(text)}catch(_:Exception){JSONObject().put("error",text.ifBlank{"HTTP ${r.code}"})};if(!r.isSuccessful)throw ApiException(r.code,obj.optString("error","Request gagal"));return obj}
}
class ApiException(val code: Int, override val message: String): Exception(message)
