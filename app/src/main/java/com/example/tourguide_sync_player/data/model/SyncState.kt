package com.example.tourguide_sync_player.data.model

data class SyncState(
    val isPlaying: Boolean = false,
    val playTime: Long = 0L,
    val seekPosition: Long = 0L,
    val mediaUrl: String? = null
)