plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val blackboxApiBaseUrl = providers.gradleProperty("blackboxApiBaseUrl")
    .orElse(providers.environmentVariable("BLACKBOX_API_BASE_URL"))
    .orElse("")
    .get()
    .trimEnd('/')

if (blackboxApiBaseUrl.isNotBlank() && !blackboxApiBaseUrl.startsWith("https://")) {
    throw GradleException("BLACKBOX API URL harus menggunakan HTTPS: $blackboxApiBaseUrl")
}

android {
    namespace = "com.blackbox.crypto"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.blackbox.crypto"
        minSdk = 26
        targetSdk = 35
        versionCode = 642
        versionName = "6.4.2"
    buildConfigField("String", "DEFAULT_API_BASE_URL", "\"$blackboxApiBaseUrl\"")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = providers.environmentVariable("ANDROID_SIGNING_STORE_FILE")
                .orNull
            val storePasswordValue = providers.environmentVariable("ANDROID_SIGNING_STORE_PASSWORD")
                .orNull
            val keyAliasValue = providers.environmentVariable("ANDROID_SIGNING_KEY_ALIAS")
                .orNull
            val keyPasswordValue = providers.environmentVariable("ANDROID_SIGNING_KEY_PASSWORD")
                .orNull

            if (!storeFilePath.isNullOrBlank()) storeFile = file(storeFilePath)
            if (!storePasswordValue.isNullOrBlank()) storePassword = storePasswordValue
            if (!keyAliasValue.isNullOrBlank()) keyAlias = keyAliasValue
            if (!keyPasswordValue.isNullOrBlank()) keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
