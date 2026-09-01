package com.example.euroamingguard

import android.Manifest
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private lateinit var switchGuard: MaterialSwitch
    private lateinit var tvGuardSubtitle: TextView
    private lateinit var tvCarrierName: TextView
    private lateinit var tvMccZone: TextView
    private lateinit var tvRoamingState: TextView
    private lateinit var cardPermissionWarning: MaterialCardView
    private lateinit var tvAdbCommand: TextView
    private lateinit var btnCopyAdb: MaterialButton
    private lateinit var btnManageCountries: MaterialButton
    private lateinit var telephonyManager: TelephonyManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> updateUI() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        initViews()
        setupListeners()
        requestAppPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun initViews() {
        switchGuard = findViewById(R.id.switchGuard)
        tvGuardSubtitle = findViewById(R.id.tvGuardSubtitle)
        tvCarrierName = findViewById(R.id.tvCarrierName)
        tvMccZone = findViewById(R.id.tvMccZone)
        tvRoamingState = findViewById(R.id.tvRoamingState)
        cardPermissionWarning = findViewById(R.id.cardPermissionWarning)
        tvAdbCommand = findViewById(R.id.tvAdbCommand)
        btnCopyAdb = findViewById(R.id.btnCopyAdb)
        btnManageCountries = findViewById(R.id.btnManageCountries)

        tvAdbCommand.text = "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
    }

    private fun setupListeners() {
        switchGuard.setOnCheckedChangeListener { _, isChecked ->
            val serviceIntent = Intent(this, RoamingGuardService::class.java)
            if (isChecked) {
                ContextCompat.startForegroundService(this, serviceIntent)
                val operatorCode = telephonyManager.networkOperator
                if (operatorCode.length >= 3) {
                    val mcc = operatorCode.substring(0, 3)
                    val isAllowed = CountryManager.isMccAllowed(this, mcc)
                    RoamingController.setRoamingEnabled(this, isAllowed)
                }
            } else {
                stopService(serviceIntent)
            }
            updateUI()
        }

        btnCopyAdb.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", tvAdbCommand.text))
            Toast.makeText(this, "ADB-kommando kopierat", Toast.LENGTH_SHORT).show()
        }

        btnManageCountries.setOnClickListener {
            startActivity(Intent(this, CountryListActivity::class.java))
        }
    }

    private fun updateUI() {
        val isRunning = isServiceRunning(RoamingGuardService::class.java)

        // Förhindra lyssnar-loop vid uppdatering av switchen
        switchGuard.setOnCheckedChangeListener(null)
        switchGuard.isChecked = isRunning
        setupListeners()

        tvGuardSubtitle.text = if (isRunning) "Guard är aktiv & övervakar nätet" else "Skyddet är inaktiverat"
        tvGuardSubtitle.setTextColor(if (isRunning) Color.parseColor("#2E7D32") else Color.GRAY)

        val hasSecure = checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        cardPermissionWarning.visibility = if (hasSecure) View.GONE else View.VISIBLE

        // Läs den faktiska hårdvarustatusen
        val isRoaming = RoamingController.isRoamingEnabled(this)
        tvRoamingState.text = if (isRoaming) "ENABLED" else "DISABLED"
        tvRoamingState.setTextColor(if (isRoaming) Color.parseColor("#2E7D32") else Color.RED)

        val operatorName = telephonyManager.networkOperatorName.ifEmpty { "Inget nätverk" }
        val operatorCode = telephonyManager.networkOperator
        tvCarrierName.text = operatorName

        if (operatorCode.length >= 3) {
            val mcc = operatorCode.substring(0, 3)
            val isAllowed = CountryManager.isMccAllowed(this, mcc)
            tvMccZone.text = if (isAllowed) "Allowed Zone (MCC $mcc)" else "Disallowed Zone (MCC $mcc)"
            tvMccZone.setTextColor(if (isAllowed) Color.parseColor("#1565C0") else Color.parseColor("#D84315"))
        } else {
            tvMccZone.text = "Söker..."
            tvMccZone.setTextColor(Color.GRAY)
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) return true
        }
        return false
    }
}
