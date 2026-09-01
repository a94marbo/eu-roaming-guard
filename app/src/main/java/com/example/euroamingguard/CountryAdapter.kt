package com.example.euroamingguard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch

class CountryAdapter(
    private var countries: List<CountryItem>,
    private val onToggle: (CountryItem, Boolean) -> Unit
) : RecyclerView.Adapter<CountryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCountryName)
        val tvMcc: TextView = view.findViewById(R.id.tvMccCode)
        val switchEnabled: MaterialSwitch = view.findViewById(R.id.switchCountry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = countries[position]
        holder.tvName.text = if (item.isCustom) "${item.name} (Custom)" else item.name
        holder.tvMcc.text = "MCC: ${item.mcc}"
        holder.switchEnabled.setOnCheckedChangeListener(null)
        holder.switchEnabled.isChecked = item.isEnabled
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            item.isEnabled = isChecked
            onToggle(item, isChecked)
        }
    }

    override fun getItemCount(): Int = countries.size

    fun updateList(newList: List<CountryItem>) {
        countries = newList
        notifyDataSetChanged()
    }
}
