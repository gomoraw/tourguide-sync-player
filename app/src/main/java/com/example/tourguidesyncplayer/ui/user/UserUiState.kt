package com.example.tourguidesyncplayer.ui.user

import android.net.nsd.NsdServiceInfo
import com.example.tourguidesyncplayer.data.model.AuthResultReason

sealed interface UserUiState {
    data object Searching : UserUiState
    data class GuideFound(val serviceInfo: NsdServiceInfo) : UserUiState
    data class Authenticating(val serviceInfo: NsdServiceInfo) : UserUiState
    data class AuthFailed(val reason: AuthResultReason, val serviceInfo: NsdServiceInfo) : UserUiState
    data class Connected(
        val guideName: String,
        val isPlaying: Boolean = false,
        val currentVideoId: String? = null,
        val videoPosition: Long = 0L,
        val playerError: String? = null
    ) : UserUiState
    data class Reconnecting(val attempt: Int, val maxAttempts: Int) : UserUiState
    data class Disconnected(val reason: String) : UserUiState
}

