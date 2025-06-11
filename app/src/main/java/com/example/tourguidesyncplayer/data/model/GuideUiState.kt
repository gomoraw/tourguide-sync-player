package com.example.tourguidesyncplayer.data.model

import java.net.InetAddress

data class VideoContent(
    val id: String,
    val title: String,
    val thumbnailUrl: String // ローカルリソース or URL
)

data class ConnectedUser(
    val id: String, // deviceId
    val deviceName: String,
    val lastSeen: Long = System.currentTimeMillis(),
    val status: ConnectionStatus = ConnectionStatus.CONNECTED
)

enum class ConnectionStatus {
    CONNECTED, // 正常 (緑)
    DELAYED,   // 遅延 (黄)
    DISCONNECTED // 切断 (赤)
}

data class GuideUiState(
    val isServiceRunning: Boolean = false,
    val localIpAddress: InetAddress? = null,
    val pinCode: String = "----",
    val connectedUsers: List<ConnectedUser> = emptyList(),
    val videoList: List<VideoContent> = emptyList(),
    val selectedVideoId: String? = null,
    val isPlaying: Boolean = false
)

