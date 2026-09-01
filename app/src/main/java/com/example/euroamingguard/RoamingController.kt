package com.example.euroamingguard

import android.content.Context
import android.provider.Settings
import android.util.Log

object RoamingController {
    private const val TAG = "RoamingController"

    fun setRoamingEnabled(context: Context, enable: Boolean): Boolean {
        val targetValue = if (enable) 1 else 0
        return try {
            val currentValue = Settings.Global.getInt(context.contentResolver, Settings.Global.DATA_ROAMING, 0)
            if (currentValue != targetValue) {
                Settings.Global.putInt(context.contentResolver, Settings.Global.DATA_ROAMING, targetValue)
                Log.i(TAG, "Data roaming set to: $enable")
            }
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing WRITE_SECURE_SETTINGS permission: ${e.message}")
            false
        }
    }

    fun isRoamingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DATA_ROAMING, 0) == 1
        } catch (e: Exception) {
            false
        }
    }
}
