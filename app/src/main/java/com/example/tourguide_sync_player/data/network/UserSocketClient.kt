package com.example.tourguide_sync_player.data.network

import com.example.tourguide_sync_player.data.model.Command
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close // close関数をインポート
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class UserSocketClient @Inject constructor(
    private val client: HttpClient
) {
    // sessionをprivateにするのは正しいが、ViewModelからisConnectedをチェックするためにFlowで公開する
    private var _session: WebSocketSession? = null
    val session: WebSocketSession?
        get() = _session // ViewModelから読み取り専用でアクセス可能にする

    private val _commandFlow = MutableSharedFlow<Command>()
    val commandFlow = _commandFlow.asSharedFlow()

    suspend fun connect(ipAddress: String, pin: String) {
        try {
            // TODO: PINコードを使った認証ロジックをここに追加
            client.webSocket(host = ipAddress, port = 8080, path = "/sync") {
                _session = this // _sessionに割り当て
                Timber.d("Connected to server.")

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val commandJson = frame.readText()
                        Timber.d("Command received: $commandJson")
                        val command = Json.decodeFromString<Command>(commandJson)
                        _commandFlow.emit(command)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Connection failed.")
            // TODO: 接続失敗時のエラーハンドリング
        } finally {
            _session = null
            Timber.d("Connection closed.")
        }
    }

    suspend fun disconnect() {
        _session?.close() // close関数を直接呼び出し
        _session = null
    }
}