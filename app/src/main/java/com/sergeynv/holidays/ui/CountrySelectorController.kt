package com.sergeynv.holidays.ui

import android.util.Log
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import com.sergeynv.holidays.data.Country

class CountrySelectorController(
    private val view: AutoCompleteTextView,
    private val debugLabel: String = "",
    private val onCountrySelected: (Country) -> Unit
) {
    init {
        // Disabling until we actually have the countries
        view.isEnabled = false
    }

    fun setUp(countries: List<Country>, preselected: Country?) = with(view) {
        require(countries.isNotEmpty())

        val adapter = CountryListFilterableAdapter(countries)
        setAdapter(adapter)

        // TODO: restore "pre-selection"!

        // onItem_Selected_Listener did not work at all, and looking at the src code it's not clear
        // why it would
        onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val country = adapter.getItem(i)
            maybeLog { "onItemClick i=$i: $country" }

            onCountrySelected(country)
        }

        setOnDismissListener {
            maybeLog { "onDismiss" }
        }

        isEnabled = true
    }

    private fun <T> T.maybeLog(messageProducer: (T) -> String): T {
        if (DEBUG) Log.d(TAG, "[$debugLabel] ${messageProducer(this)}")
        return this
    }

    companion object {
        private const val DEBUG = true
        private const val TAG = "CountrySelectorController"
    }
}