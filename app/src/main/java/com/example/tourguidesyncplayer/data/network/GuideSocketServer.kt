// パス: app/src/main/java/com/example/tourguidesyncplayer/data/network/GuideSocketServer.kt
package com.example.tourguidesyncplayer.data.network

import com.example.tourguidesyncplayer.data.model.Command
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

interface ServerCallback {
    suspend fun onClientConnected(session: DefaultWebSocketServerSession, clientId: String)
    suspend fun onClientDisconnected(clientId: String)
    suspend fun onCommandReceived(clientId: String, command: Command)
}

class GuideSocketServer(
    private val json: Json,
    private val callback: ServerCallback
) {
    private var server: NettyApplicationEngine? = null
    private val clientSessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

    fun start(port: Int) {
        // server?.application?.isActive は server?.application?.isActive でも同じ
        if (server?.application?.isActive == true) { // 修正箇所 (import io.ktor.server.application.* を追加することで解決)
            Timber.w("Server is already running.")
            return
        }
        server = embeddedServer(Netty, port = port, host = "0.0.0.0") {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
                contentConverter = KotlinxWebsocketSerializationConverter(json)
            }
            routing {
                webSocket(NetworkConstants.WEBSOCKET_PATH) {
                    val clientId = generateClientId()
                    Timber.i("Client attempting to connect with internal ID: $clientId")
                    clientSessions[clientId] = this
                    callback.onClientConnected(this, clientId)

                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val command = json.decodeFromString<Command>(frame.readText())
                                Timber.d("Received command from $clientId: $command")
                                callback.onCommandReceived(clientId, command)
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error during WebSocket session with $clientId")
                    } finally {
                        Timber.i("Client disconnected: $clientId")
                        clientSessions.remove(clientId)
                        callback.onClientDisconnected(clientId)
                    }
                }
            }
        }.start(wait = false)
        Timber.i("WebSocket server started on port $port")
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
        clientSessions.clear()
        Timber.i("WebSocket server stopped.")
    }

    suspend fun sendCommand(clientId: String, command: Command) {
        clientSessions[clientId]?.let { session ->
            try {
                session.sendSerialized(command)
                Timber.d("Sent command to $clientId: $command")
            } catch (e: Exception) {
                Timber.e(e, "Failed to send command to $clientId")
            }
        }
    }

    suspend fun broadcastCommand(command: Command) {
        Timber.i("Broadcasting command to all ${clientSessions.size} clients: $command")
        clientSessions.values.forEach { session ->
            try {
                session.sendSerialized(command)
            } catch (e: Exception) {
                Timber.w(e, "Failed to broadcast to a client. It might be disconnected.")
            }
        }
    }

    private fun generateClientId(): String {
        return "client_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}