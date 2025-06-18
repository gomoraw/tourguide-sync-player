package com.example.tourguide_sync_player.data.network

import com.example.tourguide_sync_player.data.model.Command
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation // 必要なimport
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketServerSession // 必要なimport
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.time.Duration
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuideSocketServer @Inject constructor() {

    private val clients = Collections.newSetFromMap(ConcurrentHashMap<DefaultWebSocketServerSession, Boolean>())

    private val _connectedClientCount = MutableStateFlow(0)
    val connectedClientCount = _connectedClientCount.asStateFlow()

    private val _commandFlow = MutableSharedFlow<Command>()
    val commandFlow = _commandFlow.asSharedFlow()

    private val server: ApplicationEngine = embeddedServer(Netty, port = 8080) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        install(ContentNegotiation) { // Ktor 2.x の ContentNegotiation プラグイン
            json()
        }
        routing {
            webSocket("/sync") {
                clients.add(this)
                _connectedClientCount.value = clients.size
                Timber.d("Client connected! Total clients: ${clients.size}")

                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val receivedText = frame.readText()
                            Timber.d("Received: $receivedText")
                            try {
                                val command = Json.decodeFromString<Command>(receivedText)
                                _commandFlow.emit(command)
                            } catch (e: Exception) {
                                Timber.e(e, "Error decoding command")
                            }
                        }
                    }
                } finally {
                    clients.remove(this)
                    _connectedClientCount.value = clients.size
                    Timber.d("Client disconnected! Total clients: ${clients.size}")
                }
            }
        }
    }

    fun start() {
        if (!server.isActive) { // isActiveはApplicationEngineのプロパティ
            server.start(wait = false)
            Timber.d("Server started.")
        }
    }

    fun stop() {
        if (server.isActive) {
            server.stop(1000, 5000)
            Timber.d("Server stopped.")
        }
    }

    suspend fun broadcastCommand(command: Command) {
        val commandJson = Json.encodeToString(command)
        clients.forEach { session ->
            try {
                session.send(Frame.Text(commandJson))
            } catch (e: Exception) {
                Timber.e(e, "Error broadcasting to client")
            }
        }
    }
}