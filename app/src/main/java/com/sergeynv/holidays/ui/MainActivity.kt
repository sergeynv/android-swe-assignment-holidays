package com.sergeynv.holidays.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
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
import com.sergeynv.holidays.utils.currentYear

class MainActivity : AppCompatActivity(), OnItemSelectedListener {
    private val viewModel by lazy {
        ViewModelProvider(owner = this)[HolidaysViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find and disable (until we have the list of countries) the Spinners.
        val spinnerCountryA = findViewById<Spinner>(R.id.spinner_countryA)
            .apply { isEnabled = false }
        val spinnerCountryB = findViewById<Spinner>(R.id.spinner_countryB)
            .apply { isEnabled = false }

        val fetchingIndicator: View = findViewById(R.id.progressBar_fetching)
        val holidaysListView = findViewById<RecyclerView>(R.id.recyclerView)
            .apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
            }

        with(viewModel) {
            countries.observe { countries ->
                spinnerCountryA.setUpForCountryChoice(countries, selected = holidaysHolderA.country)
                spinnerCountryB.setUpForCountryChoice(countries, selected = holidaysHolderB.country)
            }

            holidaysListView.adapter = HolidaysAdapter(
                holidaysHolderA, holidaysHolderB,
                lifecycleOwner = this@MainActivity
            )

            MediatorLiveData<Boolean>().apply {
                val onChanged = Observer<Boolean> {
                    value = holidaysHolderA.isFetching.value!! || holidaysHolderB.isFetching.value!!
                }
                addSource(holidaysHolderA.isFetching, onChanged)
                addSource(holidaysHolderB.isFetching, onChanged)
            }.observe { fetching ->
                fetchingIndicator.visibility = if (fetching) View.VISIBLE else View.GONE
            }
        }

        with(findViewById<Spinner>(R.id.spinner_year)) {
            val years = (currentYear - 10..currentYear + 10).toList()
            val selectedYear = viewModel.holidaysHolderA.year

            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, years)
            setSelection(years.indexOf(selectedYear))

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, position: Int, id: Long) {
                    val year = getItemAtPosition(position) as Int
                    viewModel.holidaysHolderA.year = year
                    viewModel.holidaysHolderB.year = year
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) =
        (parent.adapter.getItem(position) as Country?).let {
            when (parent.id) {
                R.id.spinner_countryA -> viewModel.holidaysHolderA.country = it
                R.id.spinner_countryB -> viewModel.holidaysHolderA.country = it
            }
        }

    override fun onNothingSelected(parent: AdapterView<*>) = when (parent.id) {
        R.id.spinner_countryA -> viewModel.holidaysHolderA.country = null
        R.id.spinner_countryB -> viewModel.holidaysHolderA.country = null
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

