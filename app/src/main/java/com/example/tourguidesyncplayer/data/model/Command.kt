package com.example.tourguidesyncplayer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Command {
    val type: String
}

// Server to Client (S2C) Commands
@Serializable
@SerialName("S2C_Play")
data class S2C_Play(
    val sequence: Long,
    val videoId: String
) : Command {
    override val type: String = "S2C_Play"
}

@Serializable
@SerialName("S2C_Pause")
data class S2C_Pause(
    val sequence: Long
) : Command {
    override val type: String = "S2C_Pause"
}

@Serializable
@SerialName("S2C_Sync")
data class S2C_Sync(
    val sequence: Long,
    val videoId: String,
    val isPlaying: Boolean,
    val positionMs: Long
) : Command {
    override val type: String = "S2C_Sync"
}

@Serializable
@SerialName("S2C_Ping")
data class S2C_Ping(
    val timestamp: Long
) : Command {
    override val type: String = "S2C_Ping"
}

@Serializable
@SerialName("S2C_AuthResult")
data class S2C_AuthResult(
    val success: Boolean,
    val message: String,
    val reason: AuthResultReason
) : Command {
    override val type: String = "S2C_AuthResult"
}

@Serializable
enum class AuthResultReason {
    AUTHORIZED,
    INVALID_PIN,
    RATE_LIMITED,
    INCOMPATIBLE_PROTOCOL,
    SERVER_ERROR
}

// Client to Server (C2S) Commands
@Serializable
@SerialName("C2S_Announce")
data class C2S_Announce(
    val protocolVersion: String,
    val pinCode: String,
    val deviceId: String,
    val deviceName: String
) : Command {
    override val type: String = "C2S_Announce"
}

@Serializable
@SerialName("C2S_Pong")
data class C2S_Pong(
    val timestamp: Long
) : Command {
    override val type: String = "C2S_Pong"
}

