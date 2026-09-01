package com.example.euroamingguard

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log

object RoamingController {
    private const val TAG = "RoamingController"

    /**
     * Sätter dataroaming till ON/OFF på alla kända tillverkares interna systemnycklar.
     */
    @SuppressLint("MissingPermission")
    fun setRoamingEnabled(context: Context, enable: Boolean): Boolean {
        val targetValue = if (enable) 1 else 0
        val cr = context.contentResolver

        return try {
            val keysToWrite = mutableSetOf<String>()

            // 1. Standard Android / Google Pixel / Motorola / Sony
            keysToWrite.add(Settings.Global.DATA_ROAMING)
            keysToWrite.add("data_roaming")
            keysToWrite.add("data_roaming_mode")

            // 2. Samsung One UI (Slot- & Index-baserade nycklar)
            keysToWrite.add("data_roaming0")
            keysToWrite.add("data_roaming1")
            keysToWrite.add("data_roaming2")
            keysToWrite.add("data_roaming_0")
            keysToWrite.add("data_roaming_1")
            keysToWrite.add("data_roaming_2")

            // 3. Xiaomi / Redmi / POCO (HyperOS / MIUI)
            keysToWrite.add("data_roaming_slot0")
            keysToWrite.add("data_roaming_slot1")
            keysToWrite.add("data_roaming_slot2")

            // 4. OnePlus / OPPO / Realme (ColorOS / OxygenOS)
            keysToWrite.add("oppo_data_roaming")
            keysToWrite.add("coloros_data_roaming")
            keysToWrite.add("data_roaming_sub1")
            keysToWrite.add("data_roaming_sub2")

            // 5. Huawei / Honor (EMUI / MagicOS)
            keysToWrite.add("hw_data_roaming")
            keysToWrite.add("hw_data_roaming_sim1")
            keysToWrite.add("hw_data_roaming_sim2")

            // 6. Dynamisk identifiering av aktiva SIM-kort (Fysiska SIM & eSIM)
            try {
                val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val activeSubscriptions: List<SubscriptionInfo>? = subManager?.activeSubscriptionInfoList
                if (activeSubscriptions != null) {
                    for (sub in activeSubscriptions) {
                        val subId = sub.subscriptionId
                        val slotIndex = sub.simSlotIndex

                        keysToWrite.add("data_roaming$subId")
                        keysToWrite.add("data_roaming_$subId")
                        keysToWrite.add("data_roaming_sub$subId")
                        keysToWrite.add("data_roaming_subid$subId")
                        keysToWrite.add("data_roaming_slot$slotIndex")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Kunde inte hämta aktiva SIM-kort: ${e.message}")
            }

            // Skriv targetValue (0 eller 1) till samtliga nycklar
            for (key in keysToWrite) {
                try {
                    Settings.Global.putInt(cr, key, targetValue)
                } catch (_: Exception) {
                    // Ignorera nycklar som inte stöds av just denna enhet
                }
            }

            Log.i(TAG, "Universell roaming-inställning uppdaterad till: $enable ($targetValue)")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Saknar behörighet WRITE_SECURE_SETTINGS: ${e.message}")
            false
        }
    }

    /**
     * Läser av den faktiska roaming-statusen oavsett telefontillverkare.
     */
    @SuppressLint("MissingPermission")
    fun isRoamingEnabled(context: Context): Boolean {
        val cr = context.contentResolver

        try {
            val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId()

            // 1. Kontrollera aktivt data-SIM först om tillgängligt
            if (defaultDataSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                val activeKeys = listOf(
                    "data_roaming$defaultDataSubId",
                    "data_roaming_$defaultDataSubId",
                    "data_roaming_sub$defaultDataSubId"
                )
                for (key in activeKeys) {
                    val value = Settings.Global.getInt(cr, key, -1)
                    if (value != -1) return value == 1
                }
            }

            // 2. Kontrollera alla aktiva SIM-kort och platser
            val activeList = subManager?.activeSubscriptionInfoList
            if (activeList != null) {
                for (sub in activeList) {
                    val subId = sub.subscriptionId
                    val slotIndex = sub.simSlotIndex
                    val simKeys = listOf(
                        "data_roaming$subId",
                        "data_roaming1",
                        "data_roaming2",
                        "data_roaming_slot$slotIndex",
                        "data_roaming_$slotIndex"
                    )
                    for (k in simKeys) {
                        val v = Settings.Global.getInt(cr, k, -1)
                        if (v != -1) return v == 1
                    }
                }
            }

            // 3. Kontrollera vanliga tillverkar-specifika nycklar
            val fallbackKeys = listOf(
                "data_roaming1",
                "data_roaming0",
                "data_roaming2",
                "data_roaming_0",
                "data_roaming_1",
                "oppo_data_roaming",
                "hw_data_roaming"
            )
            for (key in fallbackKeys) {
                val value = Settings.Global.getInt(cr, key, -1)
                if (value != -1) return value == 1
            }

            // 4. Global fallback (Pixel, AOSP, Motorola)
            return Settings.Global.getInt(cr, Settings.Global.DATA_ROAMING, 0) == 1
        } catch (e: Exception) {
            return false
        }
    }
}
