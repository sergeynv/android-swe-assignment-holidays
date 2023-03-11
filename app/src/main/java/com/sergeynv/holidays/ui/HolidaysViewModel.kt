package com.sergeynv.holidays.ui

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergeynv.holidays.api.HolidaysService
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.data.Holiday
import com.sergeynv.holidays.utils.currentYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HolidaysViewModel : ViewModel() {
    // "Free accounts are limited to last year's historical data only.
    // Upgrade to premium for access to all holiday data."
    // (c) HolidaysApi.com
    var year = currentYear - 1 // The "initial" value is the only year our API will serve data for.

    val countries: LiveData<List<Country>>
        get() = _countries

    var countryA: Country?
        set(value) {
            if (value != null) {
                holidaysHolderA.fetchHolidays(value, year)
            } else {
                holidaysHolderA.clear()
            }
        }
        get() = holidaysHolderA.country

    var countryB: Country?
        set(value) {
            if (value != null) {
                holidaysHolderB.fetchHolidays(value, year)
            } else {
                holidaysHolderB.clear()
            }
        }
        get() = holidaysHolderB.country

    val isFetchingHolidays: LiveData<Boolean>
    val holidays: LiveData<List<Holiday>?>

    private val holidaysService = HolidaysService.retrofit

    private val holidaysHolderA = CountryHolidaysHolder(viewModelScope, holidaysService)
    private val holidaysHolderB = CountryHolidaysHolder(viewModelScope, holidaysService)

    private val _countries = MutableLiveData<List<Country>>()

    init {
        isFetchingHolidays = MediatorLiveData<Boolean>().apply {
            val onChanged = Observer<Boolean> {
                value = holidaysHolderA.isFetching.value!! || holidaysHolderB.isFetching.value!!
            }
            addSource(holidaysHolderA.isFetching, onChanged)
            addSource(holidaysHolderB.isFetching, onChanged)
        }

        holidays = Transformations.map(isFetchingHolidays) { fetching ->
            if (fetching) null else combineHolidays()
        }

        viewModelScope.launch {
            Log.d(TAG, "Fetching country list...")

            _countries.value = holidaysService.getCountries()
                .also { Log.d(TAG, "getCountries() finished: $it") }
                .countries
        }
    }

    private fun combineHolidays(): List<Holiday>? =
        mutableListOf<Holiday>().also { combined ->
            holidaysHolderA.holidays?.let { combined.addAll(it) }
            holidaysHolderB.holidays?.let { combined.addAll(it) }
        }.takeUnless { it.isEmpty() }

    private class CountryHolidaysHolder(
        private val scope: CoroutineScope,
        private val holidaysService: HolidaysService
    ) {
        var country: Country? = null
            private set
        var holidays: List<Holiday>? = null
            private set
        val isFetching: LiveData<Boolean>
            get() = _isFetching

        private var year: Int? = null
        private var fetchingJob: Job? = null
        private val _isFetching = MutableLiveData(false)

        /**
         * Remove stored [holidays] (if any), cancel on-going [fetchingJob] (if any) and start
         * fetching holidays in the given [country] in the given [year].
         */
        @MainThread
        fun fetchHolidays(country: Country, year: Int) {
            if (country == this.country && year == this.year) return
            clear()

            this.country = country
            this.year = year

            fetchingJob = scope.launch {
                Log.d(TAG, "getHolidays(${country.code}, ${year}) starting...")
                _isFetching.value = true
                try {
                    holidays = holidaysService.getHolidays(country.code, year)
                        .also { Log.d(TAG, "getHolidays(${country.code}, $year) finished: $it") }
                        .holidays
                } finally {
                    _isFetching.value = false
                }
            }
        }

        @MainThread
        fun clear() {
            fetchingJob?.cancel()?.also {
                fetchingJob = null
            }
            country = null
            year = null
        }
    }

    companion object {
        private const val TAG = "HolidaysViewModel"
    }
}
