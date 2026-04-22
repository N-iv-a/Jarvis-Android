// Build file del modulo :app (l'applicazione vera e propria).

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.jarvis.app"
    compileSdk = 34                     // SDK di compilazione (Android 14)

    defaultConfig {
        applicationId = "com.jarvis.app"
        minSdk = 26                     // Android 8.0 — copre ~95% dei dispositivi attivi
        targetSdk = 34                  // Android 14 — comportamento runtime moderno
        versionCode = 1                 // Intero incrementale per ogni release
        versionName = "0.1.0"           // Semver leggibile per gli umani

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Garantisce che le risorse vector siano renderizzate come PNG su API vecchie
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            // Minifica codice (rimuove classi/metodi inutilizzati) + ottimizzazioni
            isMinifyEnabled = true
            // Rimuove risorse (immagini, stringhe) non referenziate
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug build: suffix per installare release e debug affiancate sullo stesso device
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        // Java 17 — richiesto da AGP 8.x
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            // Evita conflitti di file meta tra librerie
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- AndroidX core ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)

    // --- Compose (BOM = Bill of Materials, gestisce le versioni) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)

    // --- Hilt (DI) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Room + SQLCipher ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite.ktx)

    // --- Security / Crypto ---
    implementation(libs.androidx.security.crypto)

    // --- DataStore ---
    implementation(libs.androidx.datastore.preferences)

    // --- Biometric ---
    implementation(libs.androidx.biometric)

    // --- Test ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
