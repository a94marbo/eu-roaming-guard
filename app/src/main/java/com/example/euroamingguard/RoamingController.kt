package com.example.euroamingguard

import android.content.Context
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.util.Log

object RoamingController {
    private const val TAG = "RoamingController"

    fun setRoamingEnabled(context: Context, enable: Boolean): Boolean {
        val targetValue = if (enable) 1 else 0
        return try {
            val cr = context.contentResolver

            // 1. Sätt den globala inställningen
            Settings.Global.putInt(cr, Settings.Global.DATA_ROAMING, targetValue)

            // 2. Sätt SIM-specifik roaming (krävs för Android 10+ och Dual-SIM/eSIM)
            try {
                val subId = SubscriptionManager.getDefaultDataSubscriptionId()
                if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    Settings.Global.putInt(cr, "data_roaming$subId", targetValue)
                }
                Settings.Global.putInt(cr, "data_roaming1", targetValue)
                Settings.Global.putInt(cr, "data_roaming2", targetValue)
            } catch (e: Exception) {
                Log.w(TAG, "Kunde inte sätta per-SIM roaming: ${e.message}")
            }

            Log.i(TAG, "Data roaming har satts till: $enable")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Saknar WRITE_SECURE_SETTINGS: ${e.message}")
            false
        }
    }

    fun isRoamingEnabled(context: Context): Boolean {
        return try {
            val subId = SubscriptionManager.getDefaultDataSubscriptionId()
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                val subRoaming = Settings.Global.getInt(context.contentResolver, "data_roaming$subId", -1)
                if (subRoaming != -1) return subRoaming == 1
            }
            Settings.Global.getInt(context.contentResolver, Settings.Global.DATA_ROAMING, 0) == 1
        } catch (e: Exception) {
            false
        }
    }
} }
}
