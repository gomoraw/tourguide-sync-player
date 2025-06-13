// Command.kt
package com.example.tourguidesyncplayer.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Command {
    @Serializable
    data class Play(val startTime: Long, val seekPosition: Long = 0) : Command()
    @Serializable
    object Pause : Command()
    @Serializable
    data class Seek(val position: Long) : Command()
    @Serializable
    data class Load(val url: String) : Command()
}