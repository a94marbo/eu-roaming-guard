package com.example.euroamingguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.ServiceState
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat

class RoamingGuardService : Service() {

    private lateinit var telephonyManager: TelephonyManager
    private var telephonyCallback: Any? = null

    companion object {
        private const val CHANNEL_ID = "roaming_guard_channel"
        private const val NOTIF_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, createNotification("Monitoring network status..."))

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        registerNetworkListener()
    }

    private fun registerNetworkListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.ServiceStateListener {
                override fun onServiceStateChanged(serviceState: ServiceState) {
                    evaluateNetwork()
                }
            }
            telephonyManager.registerTelephonyCallback(mainExecutor, callback)
            telephonyCallback = callback
        } else {
            @Suppress("DEPRECATION")
            val listener = object : android.telephony.PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onServiceStateChanged(serviceState: ServiceState?) {
                    evaluateNetwork()
                }
            }
            @Suppress("DEPRECATION")
            telephonyManager.listen(listener, android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE)
            telephonyCallback = listener
        }
    }

    private fun evaluateNetwork() {
        val networkOperator = telephonyManager.networkOperator
        if (networkOperator.isNullOrEmpty() || networkOperator.length < 3) return

        val mcc = networkOperator.substring(0, 3)
        val isAllowed = CountryManager.isMccAllowed(this, mcc)

        if (isAllowed) {
            RoamingController.setRoamingEnabled(this, true)
            updateNotification("Connected to allowed network (MCC $mcc). Roaming ENABLED.")
        } else {
            RoamingController.setRoamingEnabled(this, false)
            updateNotification("Non-whitelisted network (MCC $mcc). Roaming DISABLED.")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "EU Roaming Guard Status", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EU Roaming Guard")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, createNotification(contentText))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyCallback is TelephonyCallback) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback as TelephonyCallback)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
