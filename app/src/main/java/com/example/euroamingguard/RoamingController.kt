package com.example.euroamingguard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log

object RoamingController {
    private const val TAG = "RoamingController"

    @SuppressLint("MissingPermission")
    fun setRoamingEnabled(context: Context, enable: Boolean): Boolean {
        val targetValue = if (enable) 1 else 0
        val cr = context.contentResolver

        return try {
            // 1. Sätt global fallback
            Settings.Global.putInt(cr, Settings.Global.DATA_ROAMING, targetValue)
            Settings.Global.putInt(cr, "data_roaming", targetValue)

            // 2. Sätt fasta SIM-platser (vanligt på Samsung / Dual-SIM)
            Settings.Global.putInt(cr, "data_roaming0", targetValue)
            Settings.Global.putInt(cr, "data_roaming1", targetValue)
            Settings.Global.putInt(cr, "data_roaming2", targetValue)
            Settings.Global.putInt(cr, "data_roaming_0", targetValue)
            Settings.Global.putInt(cr, "data_roaming_1", targetValue)
            Settings.Global.putInt(cr, "data_roaming_2", targetValue)

            // 3. Hämta alla aktiva SIM-kort och sätt deras specifika ID:n
            try {
                val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val activeList = subManager?.activeSubscriptionInfoList
                if (activeList != null) {
                    for (sub in activeList) {
                        val subId = sub.subscriptionId
                        Settings.Global.putInt(cr, "data_roaming$subId", targetValue)
                        Settings.Global.putInt(cr, "data_roaming_$subId", targetValue)
                        Settings.Global.putInt(cr, "data_roaming_sub$subId", targetValue)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Kunde inte iterera över SubscriptionManager: ${e.message}")
            }

            Log.i(TAG, "Data roaming satts till: $enable ($targetValue)")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Saknar behörighet WRITE_SECURE_SETTINGS: ${e.message}")
            false
        }
    }

    fun isRoamingEnabled(context: Context): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            // På Android 10+ (API 29+): Använd Androids officiella systemkontroll
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return telephonyManager.isDataRoamingEnabled
            }

            // Äldre versioner: Läs från Settings.Global
            Settings.Global.getInt(context.contentResolver, Settings.Global.DATA_ROAMING, 0) == 1
        } catch (e: Exception) {
            false
        }
    }
}
