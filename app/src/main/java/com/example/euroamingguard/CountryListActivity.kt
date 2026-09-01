package com.example.euroamingguard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CountryListActivity : AppCompatActivity() {

    private lateinit var rvCountries: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnReset: MaterialButton
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: CountryAdapter
    private var allCountries = mutableListOf<CountryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countries)

        rvCountries = findViewById(R.id.rvCountries)
        etSearch = findViewById(R.id.etSearch)
        btnReset = findViewById(R.id.btnResetDefaults)
        fabAdd = findViewById(R.id.fabAddCountry)

        setupRecyclerView()
        setupSearch()
        setupButtons()
    }

    private fun setupRecyclerView() {
        allCountries = CountryManager.getAllCountries(this)
        adapter = CountryAdapter(allCountries) { country, isEnabled ->
            CountryManager.setCountryEnabled(this, country.mcc, isEnabled)
        }
        rvCountries.layoutManager = LinearLayoutManager(this)
        rvCountries.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterList(query: String) {
        val filtered = allCountries.filter {
            it.name.contains(query, ignoreCase = true) || it.mcc.contains(query)
        }
        adapter.updateList(filtered)
    }

    private fun setupButtons() {
        btnReset.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Reset to Defaults")
                .setMessage("Reset the list to standard EU/EEA countries?")
                .setPositiveButton("Reset") { _, _ ->
                    CountryManager.resetToDefaults(this)
                    allCountries = CountryManager.getAllCountries(this)
                    filterList(etSearch.text.toString())
                    Toast.makeText(this, "Reset to standard EU list", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        fabAdd.setOnClickListener {
            showAddCountryDialog()
        }
    }

    private fun showAddCountryDialog() {
        val layout = layoutInflater.inflate(R.layout.dialog_add_country, null)
        val etName = layout.findViewById<EditText>(R.id.etDialogCountryName)
        val etMcc = layout.findViewById<EditText>(R.id.etDialogCountryMcc)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Allowed Country / MCC")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val mcc = etMcc.text.toString().trim()
                if (name.isNotEmpty() && mcc.length == 3 && mcc.all { it.isDigit() }) {
                    CountryManager.addCustomCountry(this, name, mcc)
                    allCountries = CountryManager.getAllCountries(this)
                    filterList(etSearch.text.toString())
                    Toast.makeText(this, "Added $name (MCC $mcc)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "MCC must be a 3-digit number", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
