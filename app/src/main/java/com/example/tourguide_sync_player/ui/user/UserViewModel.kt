package com.example.tourguide_sync_player.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourguide_sync_player.data.network.UserSocketClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val client: UserSocketClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()

    fun onPinChanged(pin: String) {
        _uiState.update { it.copy(pinCode = pin) }
    }

    fun connectToServer() {
        if (_uiState.value.isConnecting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true) }
            // TODO: IPアドレスを動的に取得する仕組みが必要。今は仮でlocalhostに。
            val ipAddress = "10.0.2.2" // エミュレータからホストPCへの特別なIP
            client.connect(ipAddress, _uiState.value.pinCode)
            // 接続後の状態管理はconnect関数内のtry-catch-finallyで行う
            // ここでは接続試行後の状態を更新
            _uiState.update { it.copy(isConnecting = false, isConnected = client.session != null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            client.disconnect()
        }
    }
}

data class UserUiState(
    val pinCode: String = "",
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val currentVideo: String? = null,
    val isPlaying: Boolean = false
)