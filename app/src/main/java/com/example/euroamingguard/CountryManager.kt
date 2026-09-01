package com.example.euroamingguard

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class CountryItem(
    val name: String,
    val mcc: String,
    var isEnabled: Boolean,
    val isCustom: Boolean = false
)

object CountryManager {
    private const val PREFS_NAME = "roaming_guard_prefs"
    private const val KEY_CUSTOM_COUNTRIES = "custom_countries_json"
    private const val KEY_ENABLED_MCCS = "enabled_mccs_set"

    private val DEFAULT_EU_COUNTRIES = listOf(
        CountryItem("Austria", "232", true),
        CountryItem("Belgium", "206", true),
        CountryItem("Bulgaria", "284", true),
        CountryItem("Croatia", "219", true),
        CountryItem("Cyprus", "280", true),
        CountryItem("Czech Republic", "230", true),
        CountryItem("Denmark", "238", true),
        CountryItem("Estonia", "248", true),
        CountryItem("Finland", "244", true),
        CountryItem("France", "208", true),
        CountryItem("Germany", "262", true),
        CountryItem("Greece", "202", true),
        CountryItem("Hungary", "216", true),
        CountryItem("Iceland (EEA)", "274", true),
        CountryItem("Ireland", "272", true),
        CountryItem("Italy", "222", true),
        CountryItem("Latvia", "247", true),
        CountryItem("Liechtenstein (EEA)", "295", true),
        CountryItem("Lithuania", "246", true),
        CountryItem("Luxembourg", "270", true),
        CountryItem("Malta", "278", true),
        CountryItem("Netherlands", "204", true),
        CountryItem("Norway (EEA)", "242", true),
        CountryItem("Poland", "260", true),
        CountryItem("Portugal", "268", true),
        CountryItem("Romania", "226", true),
        CountryItem("Slovakia", "231", true),
        CountryItem("Slovenia", "293", true),
        CountryItem("Spain", "214", true),
        CountryItem("Sweden", "240", true),
        CountryItem("Switzerland", "228", false),
        CountryItem("United Kingdom", "234", false),
        CountryItem("United Kingdom", "235", false),
        CountryItem("Andorra", "213", false),
        CountryItem("Monaco", "212", false),
        CountryItem("San Marino", "292", false),
        CountryItem("Turkey", "286", false),
        CountryItem("United States", "310", false)
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAllCountries(context: Context): MutableList<CountryItem> {
        val prefs = getPrefs(context)
        val defaultEnabledMccs = DEFAULT_EU_COUNTRIES.filter { it.isEnabled }.map { it.mcc }.toSet()
        val enabledMccs = prefs.getStringSet(KEY_ENABLED_MCCS, defaultEnabledMccs) ?: defaultEnabledMccs
        val list = mutableListOf<CountryItem>()

        for (item in DEFAULT_EU_COUNTRIES) {
            list.add(item.copy(isEnabled = enabledMccs.contains(item.mcc)))
        }

        val customJson = prefs.getString(KEY_CUSTOM_COUNTRIES, "[]") ?: "[]"
        val jsonArray = JSONArray(customJson)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val name = obj.getString("name")
            val mcc = obj.getString("mcc")
            list.add(CountryItem(name, mcc, enabledMccs.contains(mcc), isCustom = true))
        }
        return list
    }

    fun isMccAllowed(context: Context, mcc: String?): Boolean {
        if (mcc.isNullOrEmpty()) return false
        val prefs = getPrefs(context)
        val defaultEnabledMccs = DEFAULT_EU_COUNTRIES.filter { it.isEnabled }.map { it.mcc }.toSet()
        val enabledMccs = prefs.getStringSet(KEY_ENABLED_MCCS, defaultEnabledMccs) ?: defaultEnabledMccs
        return enabledMccs.contains(mcc)
    }

    fun setCountryEnabled(context: Context, mcc: String, enabled: Boolean) {
        val prefs = getPrefs(context)
        val defaultEnabledMccs = DEFAULT_EU_COUNTRIES.filter { it.isEnabled }.map { it.mcc }.toSet()
        val currentEnabled = prefs.getStringSet(KEY_ENABLED_MCCS, defaultEnabledMccs)?.toMutableSet() ?: mutableSetOf()

        if (enabled) currentEnabled.add(mcc) else currentEnabled.remove(mcc)
        prefs.edit().putStringSet(KEY_ENABLED_MCCS, currentEnabled).apply()
    }

    fun addCustomCountry(context: Context, name: String, mcc: String) {
        val prefs = getPrefs(context)
        val customJson = prefs.getString(KEY_CUSTOM_COUNTRIES, "[]") ?: "[]"
        val jsonArray = JSONArray(customJson)
        jsonArray.put(JSONObject().apply {
            put("name", name)
            put("mcc", mcc)
        })
        prefs.edit().putString(KEY_CUSTOM_COUNTRIES, jsonArray.toString()).apply()
        setCountryEnabled(context, mcc, true)
    }

    fun resetToDefaults(context: Context) {
        val prefs = getPrefs(context)
        val defaultEnabledMccs = DEFAULT_EU_COUNTRIES.filter { it.isEnabled }.map { it.mcc }.toSet()
        prefs.edit()
            .putStringSet(KEY_ENABLED_MCCS, defaultEnabledMccs)
            .remove(KEY_CUSTOM_COUNTRIES)
            .apply()
    }
}
