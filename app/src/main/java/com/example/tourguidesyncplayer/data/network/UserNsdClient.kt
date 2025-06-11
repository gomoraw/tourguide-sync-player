package com.example.tourguidesyncplayer.data.network

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserNsdClient @Inject constructor(private val nsdManager: NsdManager) {

    private var discoveryListener: NsdManager.DiscoveryListener? = null

    fun discoverServices(): Flow<NsdServiceInfo> = callbackFlow {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Timber.i("NSD discovery started.")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceName.startsWith(NetworkConstants.SERVICE_NAME_PREFIX)) {
                    Timber.i("NSD service found: $service")
                    val resolveListener = object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Timber.e("NSD resolve failed for $serviceInfo with error: $errorCode")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            Timber.i("NSD service resolved: $serviceInfo")
                            trySend(serviceInfo)
                        }
                    }
                    nsdManager.resolveService(service, resolveListener)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Timber.w("NSD service lost: $service")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Timber.i("NSD discovery stopped.")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Timber.e("NSD start discovery failed with error: $errorCode")
                close(IllegalStateException("NSD start discovery failed"))
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Timber.e("NSD stop discovery failed with error: $errorCode")
            }
        }
        
        try {
            nsdManager.discoverServices(NetworkConstants.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start NSD discovery")
            close(e)
        }

        awaitClose {
            stopDiscovery()
        }
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Timber.e(e, "Error stopping NSD discovery")
            } finally {
                discoveryListener = null
            }
        }
    }
}

