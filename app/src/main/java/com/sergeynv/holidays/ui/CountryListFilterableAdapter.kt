package com.sergeynv.holidays.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.Country

internal class CountryListFilterableAdapter(
    private val allCountries: List<Country>
) : BaseAdapter(), Filterable {
    private var filteredCountries: List<Country>? = null

    override fun getCount(): Int = (filteredCountries ?: allCountries).size

    override fun getItem(position: Int): Country = (filteredCountries ?: allCountries)[position]

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder = convertView?.tag as ViewHolder? ?: createViewHolder(parent!!)
        bindViewHolder(position, viewHolder)
        return viewHolder.view
    }

    private fun createViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.spinner_item_country, parent, false)
        return ViewHolder(view)
    }

    private fun bindViewHolder(position: Int, viewHolder: ViewHolder) = viewHolder.apply {
        val country = getItem(position)
        nameLabel.text = country.name

        // TODO: do we need to "remove" the current image or Glide will do that for us when we call
        //  load?
        // flagIcon.setImageDrawable(null)
        flagIcon.loadCountryFlag(country)
    }

    private class ViewHolder(val view: View) {
        val flagIcon: ImageView = view.findViewById(R.id.imageView_flag)
        val nameLabel: TextView = view.findViewById(R.id.textView_name)
    }

    override fun getFilter(): Filter = filter

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?) =
            FilterResults()
                .apply {
                    values = if (constraint.isNullOrBlank()) {
                        allCountries
                    } else {
                        allCountries.filter { country ->
                            country.name.contains(other = constraint, ignoreCase = true)
                        }.takeUnless { filtered -> filtered.isEmpty() }
                    }
                    count = countries?.size ?: 0
                }
                .maybeLog { "performFiltering() constraint='$constraint' -> ${it.desc}" }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) =
            results
                .maybeLog { "publishResults() constraint='$constraint' results=${it.desc}" }
                .let {
                    filteredCountries = it.countries
                    if (it.count > 0) {
                        notifyDataSetChanged()
                    } else {
                        notifyDataSetInvalidated()
                    }
                }

        override fun convertResultToString(resultValue: Any?) =
            (resultValue as? Country)?.name ?: ""

        private val FilterResults.countries: List<Country>?
            get() = (values as List<Country>?)?.takeUnless { it.isEmpty() }

        private val FilterResults.desc
            get() = "{count=$count; countries=$countries}"
    }

    companion object {
        private const val DEBUG = true
        private const val TAG = "CountryListFilterableAdapter"

        private fun <T> T.maybeLog(messageProducer: (T) -> String) =
            if (DEBUG) also { Log.d(TAG, messageProducer(this)) } else this
    }
}