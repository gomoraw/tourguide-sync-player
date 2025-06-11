// パス: app/src/main/java/com/example/tourguidesyncplayer/SyncaApplication.kt
package com.example.tourguidesyncplayer

import android.app.Application
import com.example.tourguidesyncplayer.BuildConfig // 修正箇所
import com.jakewharton.timber.Timber // 修正箇所
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SyncaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}