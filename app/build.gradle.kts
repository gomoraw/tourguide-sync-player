plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp") // kaptの代わりにkspを適用
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" // プロジェクトのKotlinバージョンに合わせる
}

android {
    namespace = "com.example.tourguide_sync_player" // ★これはandroidブロック直下でOKです
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tourguide_sync_player" // ★★★ この行はdefaultConfigブロックの内側にあります ★★★
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12" // Kotlin 1.9.23に対応
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // バージョン定義
    val hiltVersion = "2.51.1" // Hilt最新版
    val hiltNavComposeVersion = "1.2.0" // Hilt Navigation Composeは通常変わらない
    val navComposeVersion = "2.7.7" // Navigation Composeの安定版

    val ktorVersion = "2.3.10" // Ktor最新版
    val timberVersion = "5.0.1"
    val accompanistVersion = "0.32.0"
    val media3Version = "1.2.1"
    val lifecycleVersion = "2.6.2"

    // 基本ライブラリ
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00") // Compose BOMを2024.06.00に更新
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Material Components for Android (XMLテーマ用)
    implementation("com.google.android.material:material:1.12.0")

    // Hilt (DI)
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltNavComposeVersion")

    // Navigation
    implementation("androidx.navigation:navigation-compose:$navComposeVersion")

    // Ktor (WebSocket Client & Server)
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Ktor Server (WebSocket, Netty, Content Negotiation)
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")

    // Timber (Logging)
    implementation("com.jakewharton.timber:timber:$timberVersion")

    // Accompanist (Permissions)
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    // Media3 (ExoPlayer for Video)
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    // テストライブラリ
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}