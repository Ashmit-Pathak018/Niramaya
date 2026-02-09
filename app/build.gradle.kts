plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.niramaya"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.niramaya"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    // --- 1. CORE COMPOSE & ANDROID ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // --- 2. NAVIGATION FOR COMPOSE (Crucial for screen switching) ---
    // You had 'navigation-fragment' (XML) - I swapped it for 'navigation-compose'
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- 3. ICONS (For the Bottom Bar & UI) ---
    // Needed for filled icons like Home, Settings, etc.
    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    // --- 4. FIREBASE (Auth & Database) ---
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // --- 5. IMAGE LOADING (Coil for Compose) ---
    // You had 'coil' (View) - I added 'coil-compose' for AsyncImage
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- 6. CAMERAX (For scanning) ---
    val cameraVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")
    implementation("androidx.camera:camera-extensions:$cameraVersion")

    // ---8. For gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // --- 7. RETROFIT (For AI Backend) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.zxing:core:3.5.2")

    // --- 8. DEBUGGING & TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}