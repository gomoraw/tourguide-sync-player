package com.example.tourguidesyncplayer.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourguidesyncplayer.data.model.*
import com.example.tourguidesyncplayer.data.network.GuideNsdService
import com.example.tourguidesyncplayer.data.network.GuideSocketServer
import com.example.tourguidesyncplayer.data.network.NetworkConstants
import com.example.tourguidesyncplayer.data.network.ServerCallback
import com.example.tourguidesyncplayer.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
// 修正箇所: import文を追加
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

private data class AuthAttempt(val count: Int, val firstAttemptTimestamp: Long)

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val nsdService: GuideNsdService,
    private val json: Json
) : ViewModel(), ServerCallback {

    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState: StateFlow<GuideUiState> = _uiState.asStateFlow()

    private lateinit var socketServer: GuideSocketServer
    private val sequenceGenerator = AtomicLong(0)

    private val internalClientMap = ConcurrentHashMap<String, ConnectedUser>()
    private val pinAttempts = ConcurrentHashMap<String, AuthAttempt>()
    private val deviceIdToInternalId = ConcurrentHashMap<String, String>()

    private var clientMonitorJob: Job? = null

    init {
        Timber.d("GuideViewModel initialized")
        socketServer = GuideSocketServer(json, this)
        generateNewPin()
        loadVideoContent()
        startServer()
    }

    private fun generateNewPin() {
        val newPin = (1000..9999).random().toString()
        _uiState.update { it.copy(pinCode = newPin) }
        pinAttempts.clear()
        Timber.i("New PIN generated: $newPin")
    }

    private fun loadVideoContent() {
        // ダミーデータ
        val dummyVideos = listOf(
            VideoContent("video_1", "オープニング", "dummy_url"),
            VideoContent("video_2", "歴史の紹介", "dummy_url"),
            VideoContent("video_3", "メイン展示", "dummy_url")
        )
        _uiState.update { it.copy(videoList = dummyVideos) }
    }

    private fun startServer() = viewModelScope.launch(Dispatchers.IO) {
        val ipAddress = NetworkUtils.getLocalIpAddress()
        if (ipAddress == null) {
            Timber.e("Could not get local IP address. Server not started.")
            return@launch
        }

        withContext(Dispatchers.Main) {
            _uiState.update { it.copy(isServiceRunning = true, localIpAddress = ipAddress) }
        }

        try {
            socketServer.start(NetworkConstants.SERVICE_PORT)
            nsdService.registerService(NetworkConstants.SERVICE_PORT, ipAddress)
            clientMonitorJob?.cancel()
            clientMonitorJob = launch { checkClientStatus() }
            Timber.i("Server started successfully on $ipAddress")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start server")
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isServiceRunning = false) }
            }
        }
    }

    private fun stopServer() {
        clientMonitorJob?.cancel()
        nsdService.unregisterService()
        socketServer.stop()
        _uiState.update { it.copy(isServiceRunning = false, connectedUsers = emptyList()) }
        internalClientMap.clear()
        deviceIdToInternalId.clear()
        Timber.i("Server stopped.")
    }

    override suspend fun onClientConnected(session: DefaultWebSocketServerSession, clientId: String) {
        Timber.i("Internal client connected: $clientId")
    }

    override suspend fun onClientDisconnected(clientId: String) {
        Timber.i("Internal client disconnected: $clientId")
        val user = internalClientMap.remove(clientId)
        user?.let {
            deviceIdToInternalId.remove(it.id)
            updateUserList()
        }
    }

    override suspend fun onCommandReceived(clientId: String, command: Command) {
        when (command) {
            is C2S_Announce -> handleAnnounce(clientId, command)
            is C2S_Pong -> handlePong(clientId)
            else -> Timber.w("Received unexpected command from client $clientId: $command")
        }
    }

    private suspend fun handleAnnounce(clientId: String, cmd: C2S_Announce) {
        val deviceId = cmd.deviceId

        if (isRateLimited(deviceId)) {
            socketServer.sendCommand(clientId, S2C_AuthResult(false, "Rate limited", AuthResultReason.RATE_LIMITED))
            return
        }

        if (cmd.protocolVersion != NetworkConstants.PROTOCOL_VERSION) {
            socketServer.sendCommand(clientId, S2C_AuthResult(false, "Incompatible protocol", AuthResultReason.INCOMPATIBLE_PROTOCOL))
            return
        }

        if (cmd.pinCode == _uiState.value.pinCode) {
            pinAttempts.remove(deviceId)

            deviceIdToInternalId[deviceId]?.let { oldClientId ->
                internalClientMap[oldClientId]?.let { socketServer.sendCommand(oldClientId, S2C_AuthResult(false, "New connection established", AuthResultReason.SERVER_ERROR)) }
            }

            val newUser = ConnectedUser(id = deviceId, deviceName = cmd.deviceName)
            internalClientMap[clientId] = newUser
            deviceIdToInternalId[deviceId] = clientId
            updateUserList()

            socketServer.sendCommand(clientId, S2C_AuthResult(true, "Authorized", AuthResultReason.AUTHORIZED))

            val currentState = _uiState.value
            val syncCommand = S2C_Sync(
                sequence = sequenceGenerator.incrementAndGet(),
                videoId = currentState.selectedVideoId ?: currentState.videoList.firstOrNull()?.id ?: "",
                isPlaying = currentState.isPlaying,
                positionMs = 0
            )
            socketServer.sendCommand(clientId, syncCommand)

        } else {
            recordFailedAttempt(deviceId)
            socketServer.sendCommand(clientId, S2C_AuthResult(false, "Invalid PIN", AuthResultReason.INVALID_PIN))
        }
    }

    private fun handlePong(clientId: String) {
        internalClientMap[clientId]?.let { user ->
            internalClientMap.replace(clientId, user.copy(lastSeen = System.currentTimeMillis()))
        }
    }

    fun onVideoSelected(videoId: String) {
        _uiState.update { it.copy(selectedVideoId = videoId, isPlaying = true) }
        broadcastPlayCommand(videoId)
    }

    fun onPlayPauseClicked() {
        val newState = !_uiState.value.isPlaying
        _uiState.update { it.copy(isPlaying = newState) }
        if (newState) {
            _uiState.value.selectedVideoId?.let { broadcastPlayCommand(it) }
        } else {
            broadcastPauseCommand()
        }
    }

    private fun broadcastPlayCommand(videoId: String) = viewModelScope.launch {
        val command = S2C_Play(sequenceGenerator.incrementAndGet(), videoId)
        socketServer.broadcastCommand(command)
        Timber.i("Broadcasting PLAY command for video: $videoId")
    }

    private fun broadcastPauseCommand() = viewModelScope.launch {
        val command = S2C_Pause(sequenceGenerator.incrementAndGet())
        socketServer.broadcastCommand(command)
        Timber.i("Broadcasting PAUSE command")
    }

    private suspend fun checkClientStatus() = coroutineScope {
        while (isActive) {
            delay(NetworkConstants.HEARTBEAT_INTERVAL_MS / 2)
            val now = System.currentTimeMillis()
            var listChanged = false

            socketServer.broadcastCommand(S2C_Ping(now))

            internalClientMap.forEach { (clientId, user) ->
                val newStatus = when {
                    (now - user.lastSeen) > NetworkConstants.CLIENT_TIMEOUT_MS -> ConnectionStatus.DISCONNECTED
                    (now - user.lastSeen) > NetworkConstants.CLIENT_DELAYED_THRESHOLD_MS -> ConnectionStatus.DELAYED
                    else -> ConnectionStatus.CONNECTED
                }
                if (user.status != newStatus) {
                    internalClientMap.replace(clientId, user.copy(status = newStatus))
                    listChanged = true
                }
            }

            if (listChanged) {
                updateUserList()
            }
        }
    }

    private fun updateUserList() {
        val userList = internalClientMap.values.toList().sortedBy { it.deviceName }
        _uiState.update { it.copy(connectedUsers = userList) }
    }

    private fun isRateLimited(deviceId: String): Boolean {
        val attempt = pinAttempts[deviceId] ?: return false
        if (attempt.count >= NetworkConstants.MAX_PIN_ATTEMPTS) {
            if ((System.currentTimeMillis() - attempt.firstAttemptTimestamp) < NetworkConstants.RATE_LIMIT_DURATION_MS) {
                return true
            } else {
                pinAttempts.remove(deviceId)
                return false
            }
        }
        return false
    }

    private fun recordFailedAttempt(deviceId: String) {
        val now = System.currentTimeMillis()
        val currentAttempt = pinAttempts[deviceId]
        val newAttempt = if (currentAttempt == null || (now - currentAttempt.firstAttemptTimestamp) > NetworkConstants.RATE_LIMIT_DURATION_MS) {
            AuthAttempt(1, now)
        } else {
            currentAttempt.copy(count = currentAttempt.count + 1)
        }
        pinAttempts[deviceId] = newAttempt
        Timber.w("Failed PIN attempt for $deviceId. Count: ${newAttempt.count}")
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
        Timber.d("GuideViewModel cleared.")
    }
}