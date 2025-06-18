// AppModule.kt 【修正案】

package com.example.tourguide_sync_player.di

import com.example.tourguide_sync_player.data.network.UserSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(WebSockets) {
                // 必要であればWebSocketの追加設定をここに記述
            }
        }
    }

    // ★★★ この部分が不足していました ★★★
    // HttpClientを使ってUserSocketClientを生成する方法をHiltに教える
    @Provides
    @Singleton
    fun provideUserSocketClient(httpClient: HttpClient): UserSocketClient {
        return UserSocketClient(httpClient)
    }
}