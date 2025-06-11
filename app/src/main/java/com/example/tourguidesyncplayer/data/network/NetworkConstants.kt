package com.example.tourguidesyncplayer.data.network

object NetworkConstants {
    const val SERVICE_NAME_PREFIX = "TOURGUIDE_SYNC_SERVICE"
    const val SERVICE_TYPE = "_tourguide-sync._tcp."
    const val SERVICE_PORT = 58080

    // WebSocket
    const val WEBSOCKET_PATH = "/sync"
    const val PROTOCOL_VERSION = "1.0"

    // タイミング
    const val HEARTBEAT_INTERVAL_MS = 30_000L
    const val CLIENT_TIMEOUT_MS = 90_000L // 3回のハートビート失敗でタイムアウト
    const val CLIENT_DELAYED_THRESHOLD_MS = 31_000L

    // 認証
    const val MAX_PIN_ATTEMPTS = 5
    const val RATE_LIMIT_DURATION_MS = 60_000L
}

