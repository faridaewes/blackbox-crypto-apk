package com.blackbox.crypto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.blackbox.crypto.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val BbBg = Color(0xFF020705)
private val BbPanel = Color(0xFF07110C)
private val BbPanel2 = Color(0xFF0A1911)
private val BbGreen = Color(0xFF43FF9A)
private val BbGreenDim = Color(0xFF173B28)
private val BbMuted = Color(0xFF789687)
private val BbAmber = Color(0xFFD6B24D)
private val BbRed = Color(0xFFFF5968)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BlackboxApp() }
    }
}

@Composable
fun BlackboxApp() {
    val api = remember { BlackboxApi(LocalContext.current) }
    var logged by remember { mutableStateOf(api.loggedIn()) }
    var url by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mfa by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val colors = darkColorScheme(
        primary = BbGreen,
        onPrimary = Color(0xFF001A0B),
        background = BbBg,
        surface = BbPanel,
        surfaceVariant = BbPanel2,
        onSurface = Color(0xFFE9FFF1),
        onSurfaceVariant = BbMuted,
        error = BbRed
    )
    MaterialTheme(colorScheme = colors) {
        when {
            !api.configured() -> SetupScreen(url, { url = it }, {
                runCatching { api.setBaseUrl(url); error = "" }.onFailure { error = it.message ?: "URL invalid" }
            }, error)
            !logged -> LoginScreen(email, password, mfa, { email = it }, { password = it }, { mfa = it }, {
                scope.launch {
                    error = ""
                    try {
                        withContext(Dispatchers.IO) { api.login(email, password, mfa) }
                        logged = true
                    } catch (e: Exception) { error = e.message ?: "Login gagal" }
                }
            }, error)
            else -> Dashboard(api) { logged = false; api.logout() }
        }
    }
}

@Composable
private fun SetupScreen(url: String, onUrl: (String) -> Unit, onSave: () -> Unit, error: String) {
    CommandBackground {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            CoreBadge(Modifier.align(Alignment.CenterHorizontally), compact = true)
            Spacer(Modifier.height(18.dp))
            Text("BLACKBOX Crypto", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("COMMAND CENTER · Android 6.4.1", color = BbGreen, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(url, onUrl, label = { Text("Backend HTTPS URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            CommandButton("CONNECT BACKEND", onSave, Modifier.fillMaxWidth())
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun LoginScreen(email: String, password: String, mfa: String, setEmail: (String) -> Unit, setPass: (String) -> Unit, setMfa: (String) -> Unit, login: () -> Unit, error: String) {
    CommandBackground {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("SYSTEM ACCESS", color = BbGreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text("BLACKBOX COMMAND CENTER", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(email, setEmail, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(password, setPass, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(mfa, setMfa, label = { Text("MFA code (optional)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            CommandButton("AUTHENTICATE", login, Modifier.fillMaxWidth())
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun CommandBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(BbBg)) {
        val ambient = rememberInfiniteTransition(label = "bg")
        val alpha by ambient.animateFloat(.035f, .08f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "gridGlow")
        Box(Modifier.fillMaxSize().background(Color(0xFF06120B).copy(alpha = alpha)))
        content()
    }
}

@Composable
private fun CoreBadge(modifier: Modifier = Modifier, compact: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "coreBadge")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(7000), RepeatMode.Restart), label = "ring")
    val pulse by transition.animateFloat(.96f, 1.04f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val size = if (compact) 92.dp else 150.dp
    Box(modifier.size(size).graphicsLayer { scaleX = pulse; scaleY = pulse }, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().border(1.dp, BbGreen.copy(.16f), CircleShape).graphicsLayer { rotationZ = rotation })
        Box(Modifier.fillMaxSize(.82f).border(1.dp, BbGreen.copy(.25f), CircleShape).graphicsLayer { rotationZ = -rotation * .7f })
        Box(Modifier.fillMaxSize(.60f).background(BbPanel2, CircleShape).border(1.dp, BbGreen, CircleShape), contentAlignment = Alignment.Center) {
            Text("◉", color = BbGreen, fontSize = if (compact) 30.sp else 42.sp)
        }
    }
}

@Composable
private fun Dashboard(api: BlackboxApi, logout: () -> Unit) {
    var bots by remember { mutableStateOf<List<Bot>>(emptyList()) }
    var tier by remember { mutableStateOf("DEMO") }
    var signals by remember { mutableStateOf<List<Signal>>(emptyList()) }
    var risk by remember { mutableStateOf("Loading…") }
    var maintenance by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var msg by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf("COMMAND") }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            busy = true
            try {
                val data = withContext(Dispatchers.IO) { api.bots() }
                bots = data.first
                tier = data.second.optString("label", "DEMO")
                signals = withContext(Dispatchers.IO) { api.signals() }
                val riskObj = withContext(Dispatchers.IO) { api.risk() }
                risk = riskObj.toString(2)
                maintenance = riskObj.optJSONObject("global")?.optBoolean("maintenance", false) == true
                notifications = withContext(Dispatchers.IO) { api.notifications() }
            } catch (e: Exception) { msg = e.message ?: "Request failed" }
            finally { busy = false }
        }
    }
    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        containerColor = BbBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BLACKBOX", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("COMMAND CENTER", color = BbGreen, fontSize = 9.sp, letterSpacing = 1.8.sp)
                    }
                },
                actions = { TextButton(logout) { Text("EXIT") } }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF030A07)) {
                listOf("COMMAND", "BOTS", "SIGNALS", "LINKS").forEach { item ->
                    NavigationBarItem(selected = selectedSection == item, onClick = { selectedSection = item }, icon = { Text(if (item == "COMMAND") "◉" else if (item == "BOTS") "▣" else if (item == "SIGNALS") "⌁" else "◈", fontSize = 20.sp) }, label = { Text(item, fontSize = 8.sp) })
                }
            }
        }
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                CommandHeader(tier, busy, refresh, logout)
            }
            if (maintenance) {
                item { MaintenanceCard() }
            }
            if (notifications.isNotEmpty()) {
                item { NotificationPanel(notifications) }
            }
            item {
                when (selectedSection) {
                    "COMMAND" -> CommandCorePanel(bots.firstOrNull(), tier, risk)
                    "BOTS" -> Text("BOT FLEET", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    "SIGNALS" -> Text("LIVE SIGNALS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    else -> Text("CONNECTIONS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            if (selectedSection == "COMMAND") {
                item { AlmaiAdvisorCard() }
                item { ConnectionsCard(api) { msg = it } }
                item { RiskPanel(risk) }
                item { Text("BOT FLEET", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                items(bots) { bot -> BotCard(api, bot, { refresh() }, maintenance) { msg = it } }
                item { Text("LIVE SIGNALS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                items(signals.take(8)) { s -> SignalCard(s) }
            } else if (selectedSection == "BOTS") {
                items(bots) { bot -> BotCard(api, bot, { refresh() }, maintenance) { msg = it } }
            } else if (selectedSection == "SIGNALS") {
                items(signals) { s -> SignalCard(s) }
            } else {
                item { ConnectionsCard(api) { msg = it } }
                item { SecurityLinkCard(api) }
            }
            if (msg.isNotBlank()) item { AnimatedVisibility(true) { Text(msg, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) } }
            item { Text("Server-authoritative: risk, execution, tier, wallet/exchange state. Android never stores private keys or exchange secrets.", color = BbMuted, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun CommandHeader(tier: String, busy: Boolean, refresh: () -> Unit, logout: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = BbPanel, border = BorderStroke(1.dp, BbGreenDim)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(8.dp).background(BbGreen, CircleShape))
            Column(Modifier.weight(1f)) {
                Text("SYSTEM ONLINE", color = BbGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                Text("Tier: $tier", color = BbMuted, fontSize = 11.sp)
            }
            OutlinedButton(onClick = refresh, enabled = !busy, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) { Text(if (busy) "SYNC…" else "SYNC") }
        }
    }
}

@Composable
private fun CommandCorePanel(bot: Bot?, tier: String, risk: String) {
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFF06100B), border = BorderStroke(1.dp, BbGreenDim), tonalElevation = 2.dp) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CoreBadge()
            Spacer(Modifier.height(10.dp))
            Text(bot?.let { if (it.status == "RUNNING") "CORE ONLINE" else if (it.status == "PAUSED") "CORE PAUSED" else "CORE STANDBY" } ?: "CORE STANDBY", color = BbGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
            Text(bot?.let { "${it.name} · ${it.mode}" } ?: "No active bot", color = BbMuted, fontSize = 11.sp)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("TIER", tier, Modifier.weight(1f))
                StatusChip("RISK", if (risk.contains("halt", true)) "HALT" else "ARMED", Modifier.weight(1f))
                StatusChip("ENGINE", "READY", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(10.dp), color = Color(0xFF07150E), border = BorderStroke(1.dp, BbGreenDim)) {
        Column(Modifier.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = BbMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(value, color = BbGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SignalCard(s: Signal) {
    Surface(shape = RoundedCornerShape(13.dp), color = BbPanel, border = BorderStroke(1.dp, BbGreenDim)) {
        Row(Modifier.fillMaxWidth().padding(13.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(if (s.action == "BUY") BbGreen else if (s.action == "SELL") BbRed else BbAmber, CircleShape))
            Column(Modifier.weight(1f)) { Text("${s.symbol} · ${s.action}", fontWeight = FontWeight.Bold); Text("Score ${s.score} · Confidence ${s.confidence}%", color = BbMuted, fontSize = 11.sp) }
            Text("LIVE", color = BbGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RiskPanel(risk: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = BbPanel, border = BorderStroke(1.dp, BbGreenDim)) {
        Column(Modifier.padding(15.dp)) { Text("RISK / OPERATIONS", color = BbGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(Modifier.height(6.dp)); Text(risk.take(1400), color = BbMuted, fontSize = 10.sp, lineHeight = 15.sp) }
    }
}

@Composable
private fun SecurityLinkCard(api: BlackboxApi) {
    val context = LocalContext.current
    Surface(shape = RoundedCornerShape(16.dp), color = BbPanel, border = BorderStroke(1.dp, BbGreenDim)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("SECURITY CENTER", fontWeight = FontWeight.Bold); Text("Session, MFA and account controls remain on the production backend.", color = BbMuted, fontSize = 10.sp) }
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(api.baseUrl() + "/dashboard/security"))) }) { Text("OPEN") }
        }
    }
}

@Composable
private fun AlmaiAdvisorCard() {
    val context = LocalContext.current
    Surface(shape = RoundedCornerShape(16.dp), color = BbPanel2, border = BorderStroke(1.dp, BbGreenDim)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = "https://almai.id/images/logo-alma-3D.gif", contentDescription = "Logo ALMAI", modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f)) { Text("Penasihat Berjangka", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("ALMAI · layanan penasihat", color = BbMuted, style = MaterialTheme.typography.bodySmall) }
            Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://almai.id/referral/Indonesia"))) }) { Text("DAFTAR") }
        }
    }
}

@Composable
private fun NotificationPanel(items: List<AppNotification>) {
    Surface(shape = RoundedCornerShape(18.dp), color = BbPanel, border = BorderStroke(1.dp, BbGreenDim)) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SYSTEM NOTIFICATIONS", color = BbGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f)); Text("${items.size}", color = BbMuted, fontSize = 10.sp)
            }
            items.take(3).forEach { n ->
                Column(Modifier.fillMaxWidth().border(1.dp, BbGreenDim, RoundedCornerShape(10.dp)).padding(10.dp)) {
                    Text(n.title, fontWeight = FontWeight.Bold, color = if (n.priority == "URGENT") BbAmber else MaterialTheme.colorScheme.onSurface)
                    Text(n.body, color = BbMuted, fontSize = 10.sp, maxLines = 5, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun MaintenanceCard() {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF201707), border = BorderStroke(1.dp, BbAmber.copy(.55f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("MAINTENANCE AKTIF", color = BbAmber, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text("Trading baru dihentikan dan bot yang berjalan telah dihentikan otomatis.", color = Color(0xFFFFE6B0))
            Text("Posisi terbuka tidak dilikuidasi paksa; protective exit dan reconciliation tetap mengikuti safety engine.", color = BbMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun BotCard(api: BlackboxApi, bot: Bot, refresh: () -> Unit, maintenance: Boolean, error: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "bot-${bot.id}")
    val pulse by transition.animateFloat(.98f, 1.02f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "botPulse")
    Surface(shape = RoundedCornerShape(17.dp), color = BbPanel, border = BorderStroke(if (bot.status == "RUNNING") 1.5.dp else 1.dp, if (bot.status == "RUNNING") BbGreen.copy(.55f) else BbGreenDim)) {
        Column(Modifier.fillMaxWidth().padding(15.dp).graphicsLayer { if (bot.status == "RUNNING") { scaleX = pulse; scaleY = pulse } }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(bot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("${bot.status} · ${bot.mode}", color = if (bot.status == "RUNNING") BbGreen else BbMuted, fontSize = 11.sp) }
                Text(if (bot.status == "RUNNING") "LIVE" else "IDLE", color = if (bot.status == "RUNNING") BbGreen else BbMuted, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                CommandButton(if (bot.status == "RUNNING") "PAUSE" else "START", {
                    scope.launch { running = true; try { withContext(Dispatchers.IO) { api.control(bot.id, if (bot.status == "RUNNING") "PAUSE" else "START", bot.status != "RUNNING") }; refresh() } catch (e: Exception) { error(e.message ?: "Control failed") }; running = false }
                }, Modifier.weight(1f), enabled = !running && !maintenance)
                CommandButton("STOP", { scope.launch { try { withContext(Dispatchers.IO) { api.control(bot.id, "STOP") }; refresh() } catch (e: Exception) { error(e.message ?: "STOP failed") } } }, Modifier.weight(1f), enabled = bot.status != "STOPPED" && !maintenance)
                CommandButton("CLOSE", { scope.launch { try { withContext(Dispatchers.IO) { api.closeAll(bot.id) }; refresh() } catch (e: Exception) { error(e.message ?: "Close failed") } } }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(7.dp))
            OutlinedButton(onClick = { scope.launch { if (bot.status != "STOPPED") { error("Bot harus STOPPED sebelum diarsipkan"); return@launch }; try { withContext(Dispatchers.IO) { api.deleteBot(bot.id) }; refresh() } catch (e: Exception) { error(e.message ?: "Archive failed") } } }, enabled = bot.status == "STOPPED" && !maintenance, modifier = Modifier.fillMaxWidth()) { Text("ARCHIVE BOT") }
        }
    }
}

@Composable
private fun CommandButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 44.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A2516), contentColor = BbGreen)) { Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp) }
}

@Composable
private fun ConnectionsCard(api: BlackboxApi, error: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exchanges by remember { mutableStateOf<List<ExchangeConnection>>(emptyList()) }
    var wallets by remember { mutableStateOf<List<WalletConnection>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var mfaCode by remember { mutableStateOf("") }
    var showExchange by remember { mutableStateOf(false) }
    var provider by remember { mutableStateOf("") }; var label by remember { mutableStateOf("") }; var apiKey by remember { mutableStateOf("") }; var secret by remember { mutableStateOf("") }; var passphrase by remember { mutableStateOf("") }; var uid by remember { mutableStateOf("") }
    var testnet by remember { mutableStateOf(false) }; var futures by remember { mutableStateOf(false) }
    fun refresh() { scope.launch { try { busy = true; exchanges = withContext(Dispatchers.IO) { api.exchangeConnections() }; wallets = withContext(Dispatchers.IO) { api.walletConnections() } } catch (e: Exception) { error(e.message ?: "Connection load failed") } finally { busy = false } } }
    LaunchedEffect(Unit) { refresh() }
    Surface(shape = RoundedCornerShape(17.dp), color = BbPanel, border = BorderStroke(1.dp, BbGreenDim)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text("CONNECTIONS", fontWeight = FontWeight.Bold, color = BbGreen, letterSpacing = 1.sp); Spacer(Modifier.weight(1f)); Text("MFA GATED", color = BbMuted, fontSize = 9.sp) }
            Text("Exchange API secret hanya dikirim ke backend HTTPS dan tidak disimpan di APK.", color = BbMuted, fontSize = 10.sp)
            OutlinedTextField(mfaCode, { mfaCode = it.filter(Char::isDigit).take(6) }, label = { Text("MFA 6 digit") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                CommandButton("CONNECT EXCHANGE", { showExchange = true }, Modifier.weight(1f), enabled = mfaCode.length == 6 && !busy)
                OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(api.baseUrl() + "/dashboard/connections"))) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("WALLET / WEB") }
            }
            exchanges.filter { it.status == "CONNECTED" }.forEach { c -> ConnectionRow(c.provider, "${c.label} · ${c.marketType}${if (c.testnet) " · TESTNET" else ""}", !busy && mfaCode.length == 6) { scope.launch { try { busy = true; withContext(Dispatchers.IO) { api.disconnectExchange(c.id, mfaCode) }; refresh() } catch (e: Exception) { error(e.message ?: "Disconnect failed") } finally { busy = false } } } }
            wallets.filter { it.status == "CONNECTED" }.forEach { w -> ConnectionRow(w.provider, "${w.chain} · ${w.address.take(8)}…${w.address.takeLast(6)}", !busy && mfaCode.length == 6) { scope.launch { try { busy = true; withContext(Dispatchers.IO) { api.disconnectWallet(w.id, mfaCode) }; refresh() } catch (e: Exception) { error(e.message ?: "Disconnect failed") } finally { busy = false } } } }
        }
    }
    if (showExchange) AlertDialog(
        onDismissRequest = { showExchange = false },
        title = { Text("CONNECT EXCHANGE") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(provider, { provider = it }, label = { Text("Provider ID, contoh BINANCE") }); OutlinedTextField(label, { label = it }, label = { Text("Label") }); OutlinedTextField(apiKey, { apiKey = it }, label = { Text("API Key") }); OutlinedTextField(secret, { secret = it }, label = { Text("Secret") }); OutlinedTextField(passphrase, { passphrase = it }, label = { Text("Passphrase jika perlu") }); OutlinedTextField(uid, { uid = it }, label = { Text("UID jika perlu") })
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(testnet, { testnet = it }); Text("Testnet") }; Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(futures, { futures = it }); Text("Futures") }
        } },
        confirmButton = { CommandButton("CONNECT", { scope.launch { try { busy = true; withContext(Dispatchers.IO) { api.connectExchange(provider, label, apiKey, secret, passphrase, uid, testnet, !futures, futures, mfaCode) }; showExchange = false; provider = ""; label = ""; apiKey = ""; secret = ""; passphrase = ""; uid = ""; refresh() } catch (e: Exception) { error(e.message ?: "Connect failed") } finally { busy = false } } }, enabled = provider.isNotBlank() && label.isNotBlank() && apiKey.isNotBlank() && secret.isNotBlank() && !busy) },
        dismissButton = { TextButton(onClick = { showExchange = false }) { Text("CANCEL") } }
    )
}

@Composable
private fun ConnectionRow(name: String, detail: String, enabled: Boolean, disconnect: () -> Unit) {
    Row(Modifier.fillMaxWidth().border(1.dp, BbGreenDim, RoundedCornerShape(10.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.Bold); Text(detail, color = BbMuted, fontSize = 10.sp) }
        TextButton(onClick = disconnect, enabled = enabled) { Text("DISCONNECT", color = BbRed, fontSize = 9.sp) }
    }
}
