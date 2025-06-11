package com.example.tourguidesyncplayer.di

import android.content.Context
import android.net.nsd.NsdManager
import com.example.tourguidesyncplayer.data.model.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            polymorphic(Command::class) {
                subclass(S2C_Play::class, S2C_Play.serializer())
                subclass(S2C_Pause::class, S2C_Pause.serializer())
                subclass(S2C_Sync::class, S2C_Sync.serializer())
                subclass(S2C_Ping::class, S2C_Ping.serializer())
                subclass(S2C_AuthResult::class, S2C_AuthResult.serializer())
                subclass(C2S_Announce::class, C2S_Announce.serializer())
                subclass(C2S_Pong::class, C2S_Pong.serializer())
            }
        }
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient(CIO) {
            install(WebSockets)
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    @Provides
    @Singleton
    fun provideNsdManager(@ApplicationContext context: Context): NsdManager {
        return context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }
}

