package com.example.tourguidesyncplayer.util

import timber.log.Timber
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {
    fun getLocalIpAddress(): InetAddress? {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
            for (intf in networkInterfaces) {
                val addrs = intf.inetAddresses.toList()
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr
                    }
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to get local IP address")
        }
        return null
    }
}

