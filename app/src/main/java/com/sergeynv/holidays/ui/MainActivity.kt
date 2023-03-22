package com.sergeynv.holidays.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
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

        // Set up filter (strategy) selector.
        val filterSelectorInputLayout = findViewById<TextInputLayout>(R.id.filterSelectorInputLayout)
        val filterSelector = filterSelectorInputLayout.findViewById<AutoCompleteTextView>(R.id.filterSelector)
            .apply {
                (parent as View).isEnabled = false
                val adapter = ArrayAdapter(context, R.layout.list_item_filter_option,
                    HolidaysFilterStrategy.getDescriptions(context))
                setAdapter(adapter)

                onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
                    HolidaysFilterStrategy.values()[i].let {
                        viewModel.selectedHolidaysFilterStrategy = it
                        holidaysAdapter.filterStrategy = it
                    }
                }
            }

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = holidaysAdapter
        }

        val fetchingIndicator: View = findViewById(R.id.progressBar_fetching)
        with(viewModel) {
            val countrySelectorA = countrySelectorController(R.id.countrySelectorA, "A") {
                countryA = it
            }
            val countrySelectorB = countrySelectorController(R.id.countrySelectorB, "B") {
                countryB = it
            }

            countries.observe { countries ->
                countrySelectorA.setUp(countries, preselected = countryA)
                countrySelectorB.setUp(countries, preselected = countryB)
            }

            holidays.observe { holidaysAdapter.yearHolidays = it }

            selectedHolidaysFilterStrategy.let {
                // Set the default, or restore previously selected, filtering option.
                filterSelector.setText(it.getDescription(context), /* filter */ false)
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
                // Generally, we want to allow the user to choose the filtering strategy only if
                // holidays for both countries are loaded.
                filterSelectorInputLayout.isEnabled = countryA != null && countryB != null
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

    private fun countrySelectorController(
        @IdRes id: Int,
        debugLabel: String,
        listener: (Country?) -> Unit
    ) = CountrySelectorController(findViewById(id), debugLabel, listener)

    private fun <T> LiveData<T>.observe(observer: Observer<T>) =
        observe(activityLifecycleOwner, observer)

    private val activityLifecycleOwner: LifecycleOwner
        get() = this

    companion object {
        private const val TAG = "MainActivity"
    }
}
