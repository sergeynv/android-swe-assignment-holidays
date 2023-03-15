package com.sergeynv.holidays.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.Country

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(owner = this)[HolidaysViewModel::class.java]
    }
    private val holidaysAdapter = HolidaysAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val context = this

        // Set up spinner with filter (strategy) options.
        val spinnerFilterStrategy = findViewById<Spinner>(R.id.spinner_filterStrategy)
            .apply {
                isEnabled = false
                adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item,
                    HolidaysFilterStrategy.getDescriptions(context)
                )
                onItemSelected { position ->
                    HolidaysFilterStrategy.values()[position].let {
                        viewModel.selectedHolidaysFilterStrategy = it
                        holidaysAdapter.filterStrategy = it
                    }
                }
            }

        // Find and disable (until we have the list of countries) the Spinners.
        val spinnerCountryA = findViewById<Spinner>(R.id.spinner_countryA)
            .apply { isEnabled = false }
        val spinnerCountryB = findViewById<Spinner>(R.id.spinner_countryB)
            .apply { isEnabled = false }

        val fetchingIndicator: View = findViewById(R.id.progressBar_fetching)

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = holidaysAdapter
        }

        with(viewModel) {
            countries.observe { countries ->
                spinnerCountryA.setUpForCountryChoice(countries, preselected = countryA)
                spinnerCountryB.setUpForCountryChoice(countries, preselected = countryB)
            }

            holidays.observe { holidaysAdapter.yearHolidays = it }

            selectedHolidaysFilterStrategy.let {
                spinnerFilterStrategy.setSelection(it.ordinal)
                holidaysAdapter.filterStrategy = it
            }

            MediatorLiveData<Boolean>().apply {
                val onChanged = Observer<Boolean> {
                    value = isFetchingCountries.value!! || isFetchingHolidays.value!!
                }
                addSource(isFetchingCountries, onChanged)
                addSource(isFetchingHolidays, onChanged)
            }.observe { fetching ->
                fetchingIndicator.visibility = if (fetching) View.VISIBLE else View.GONE

                // Cheating a bit here, normally would create another MediatorLiveData.
                // Generally we want to allow the user to choose the filtering strategy only if
                // holidays for both countries are loaded.
                spinnerFilterStrategy.isEnabled = countryA != null && countryB != null
            }
        }

        with(findViewById<Spinner>(R.id.spinner_year)) {
            val years = (currentYear - 10..currentYear + 10).toList()
            val selectedYear = viewModel.year

            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, years)
            setSelection(years.indexOf(selectedYear))

            onItemSelected { position ->
                viewModel.year = getItemAtPosition(position) as Int
            }
        }
    }

    private fun Spinner.setUpForCountryChoice(countries: List<Country>, preselected: Country?) {
        // 1. Create and set the adapter
        val countryListAdapter = CountryListSpinnerAdapter(countries)
        adapter = countryListAdapter

        // 2. Set (restore) Spinner selection.
        setSelection(countryListAdapter.getItemPosition(preselected))

        // 3. Now that we restored the selection, set the selection listener.
        onItemSelected { position ->
            countryListAdapter.getItem(position).let {
                when (id) {
                    R.id.spinner_countryA -> viewModel.countryA = it
                    R.id.spinner_countryB -> viewModel.countryB = it
                    else -> throw RuntimeException("Unexpected Spinner layout id")
                }
            }
        }

        // 4. And finally let users to make selections (enable the Spinner).
        isEnabled = true
    }

    private fun <T> LiveData<T>.observe(observer: Observer<T>) =
        observe(this@MainActivity, observer)

    companion object {
        private const val TAG = "MainActivity"
    }
}
