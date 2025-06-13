// ServerCallback.kt
package com.example.tourguidesyncplayer.data

import com.example.tourguidesyncplayer.data.model.Command

interface ServerCallback {
    fun onCommandReceived(command: Command)
}