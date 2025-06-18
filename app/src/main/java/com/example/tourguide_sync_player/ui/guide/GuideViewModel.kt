package com.example.tourguide_sync_player.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourguide_sync_player.data.model.Command // 必要なimport
import com.example.tourguide_sync_player.data.model.SyncState // 必要なimport
import com.example.tourguide_sync_player.data.network.GuideSocketServer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val server: GuideSocketServer
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState = _uiState.asStateFlow()

    init {
        startServer()
        observeServerCommands()
        observeClientCount()
    }

    private fun startServer() {
        server.start()
    }

    private fun stopServer() {
        server.stop()
    }

    private fun observeClientCount() {
        server.connectedClientCount
            .onEach { count ->
                _uiState.update { it.copy(connectedClients = count) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeServerCommands() {
        server.commandFlow
            .onEach { command ->
                onCommandReceived(command)
            }
            .launchIn(viewModelScope)
    }

    fun onPlayPauseClick() {
        viewModelScope.launch {
            val command = if (uiState.value.syncState.isPlaying) {
                Command.Pause
            } else {
                Command.Play(System.currentTimeMillis())
            }
            server.broadcastCommand(command)
            onCommandReceived(command)
        }
    }

    private fun onCommandReceived(command: Command) {
        Timber.d("ViewModel received command: $command")
        _uiState.update { currentState ->
            val newSyncState = when (command) {
                is Command.Play -> currentState.syncState.copy(
                    isPlaying = true,
                    playTime = command.startTime,
                    seekPosition = command.seekPosition
                )
                is Command.Pause -> currentState.syncState.copy(
                    isPlaying = false
                )
                is Command.Seek -> currentState.syncState.copy(
                    seekPosition = command.position
                )
                is Command.Load -> currentState.syncState.copy(
                    mediaUrl = command.url
                )
                // 'when'式が網羅的でないエラー対策:
                // Commandがsealed classなので、全てのサブクラスをwhenで処理する必要があります。
                // 新しいサブクラスが追加されたらここに追加するか、
                // それが望ましくない場合は 'else -> currentState.syncState' を追加します。
                else -> currentState.syncState // デフォルトのelseブランチを追加
            }
            currentState.copy(syncState = newSyncState)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
    }
}

data class GuideUiState(
    val connectedClients: Int = 0,
    val syncState: SyncState = SyncState()
)