// build.gradle.kts (Project) 【最新互換版】
plugins {
    // compileSdk 34 と互換性のあるAGP 8.2.2 を使用
    id("com.android.application") version "8.2.2" apply false
    // AGP 8.2.2 と互換性のある Kotlin 1.9.22 を使用
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false // Hiltは安定版のまま
}