// GuideViewModel.kt【最新版】
package com.example.tourguidesyncplayer.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourguidesyncplayer.data.ServerCallback
import com.example.tourguidesyncplayer.data.model.Command
import com.example.tourguidesyncplayer.data.model.SyncState
import com.example.tourguidesyncplayer.data.network.GuideSocketServer
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
) : ViewModel(), ServerCallback {

    private val _uiState = MutableStateFlow(SyncState())
    val uiState = _uiState.asStateFlow()

    init {
        startServer()
        observeServerCommands()
    }

    private fun startServer() {
        server.start()
    }

    private fun stopServer() {
        server.stop()
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
            val command = if (uiState.value.isPlaying) {
                Command.Pause
            } else {
                Command.Play(System.currentTimeMillis())
            }
            server.broadcastCommand(command)
            onCommandReceived(command) // ローカルでも状態を更新
        }
    }

    override fun onCommandReceived(command: Command) {
        Timber.d("ViewModel received command: $command")
        _uiState.update { currentState ->
            when (command) {
                is Command.Play -> currentState.copy(
                    isPlaying = true,
                    playTime = command.startTime,
                    seekPosition = command.seekPosition
                )
                is Command.Pause -> currentState.copy(
                    isPlaying = false
                )
                is Command.Seek -> currentState.copy(
                    seekPosition = command.position
                )
                is Command.Load -> currentState.copy(
                    mediaUrl = command.url
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
    }
}