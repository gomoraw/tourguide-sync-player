package com.example.tourguidesyncplayer.data.network

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import java.net.InetAddress
import javax.inject.Inject

@ViewModelScoped
class GuideNsdService @Inject constructor(private val nsdManager: NsdManager) {

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var registeredServiceName: String? = null

    fun registerService(port: Int, host: InetAddress) {
        if (registrationListener != null) {
            Timber.w("Service is already registered or registration is in progress.")
            return
        }

        val serviceInfo = NsdServiceInfo().apply {
            // サービス名は一意である必要があるため、デバイス名などを付加する
            serviceName = "${NetworkConstants.SERVICE_NAME_PREFIX}_${Build.MODEL.replace(" ", "_")}"
            serviceType = NetworkConstants.SERVICE_TYPE
            setPort(port)
            setHost(host)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
                registeredServiceName = nsdServiceInfo.serviceName
                Timber.i("NSD Service registered: $registeredServiceName on port ${nsdServiceInfo.port}")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Timber.e("NSD Service registration failed with error code: $errorCode")
                registrationListener = null
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                Timber.i("NSD Service unregistered: ${arg0.serviceName}")
                registrationListener = null
                registeredServiceName = null
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Timber.e("NSD Service unregistration failed with error code: $errorCode")
                registrationListener = null
            }
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initiate NSD service registration")
        }
    }

    fun unregisterService() {
        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: Exception) {
                Timber.e(e, "Error while unregistering NSD service")
            } finally {
                registrationListener = null
                registeredServiceName = null
            }
        }
    }
}

