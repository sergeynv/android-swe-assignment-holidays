package com.sergeynv.holidays.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.utils.currentYear

class MainActivity : AppCompatActivity(), OnItemSelectedListener {
    private lateinit var spinnerCountryA: Spinner
    private lateinit var spinnerCountryB: Spinner

    private val viewModel by lazy {
        ViewModelProvider(owner = this)[HolidaysViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(findViewById<Spinner>(R.id.spinner_year)) {
            val years = (currentYear - 10..currentYear + 10).toList()
            val selectedYear = viewModel.year

            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, years)
            setSelection(years.indexOf(selectedYear))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, position: Int, id: Long) {
                    viewModel.year = getItemAtPosition(position) as Int
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */ }
            }
        }

        // Find and disable (until we have the list of countries) the Spinners.
        spinnerCountryA = findViewById<Spinner>(R.id.spinner_countryA)
            .apply { isEnabled = false }
        spinnerCountryB = findViewById<Spinner>(R.id.spinner_countryB)
            .apply { isEnabled = false }

        val fetchingIndicator: View = findViewById(R.id.progressBar_fetching)

        val holidaysAdapter = HolidaysAdapter()

        findViewById<RecyclerView>(R.id.recyclerView)
            .apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = holidaysAdapter
            }

        with(viewModel) {
            countries.observe { countries ->
                spinnerCountryA.setUpForCountryChoice(countries = countries, selected = countryA)
                spinnerCountryB.setUpForCountryChoice(countries = countries, selected = countryB)
            }

            isFetchingHolidays.observe { fetching ->
                fetchingIndicator.visibility = if (fetching) View.VISIBLE else View.GONE
            }

            holidays.observe {
                holidaysAdapter.holidays = it ?: emptyList()
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) =
        (parent.adapter.getItem(position) as Country?).let {
            when (parent.id) {
                R.id.spinner_countryA -> viewModel.countryA = it
                R.id.spinner_countryB -> viewModel.countryB = it
            }
        }

    override fun onNothingSelected(parent: AdapterView<*>) = when (parent.id) {
        R.id.spinner_countryA -> viewModel.countryA = null
        R.id.spinner_countryB -> viewModel.countryB = null
        else -> Unit
    }

    private fun Spinner.setUpForCountryChoice(countries: List<Country>, selected: Country?) {
        adapter = CountryListSpinnerAdapter(countries).also { adapter ->
            // Set (restore) Spinner selection.
            setSelection(adapter.getItemPosition(selected))
        }
        // Now that we restored the selection, let's set the selection listener...
        onItemSelectedListener = this@MainActivity
        // ... and finally let users to make selections (enable the Spinner).
        isEnabled = true
    }

    private fun <T> LiveData<T>.observe(observer: Observer<T>) =
        observe(this@MainActivity, observer)

    companion object {
        private const val TAG = "MainActivity"
    }
}

