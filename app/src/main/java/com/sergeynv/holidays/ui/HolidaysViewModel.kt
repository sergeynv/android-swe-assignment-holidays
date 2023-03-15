package com.sergeynv.holidays.ui

import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergeynv.holidays.HolidaysApplication
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.data.HolidaysRepository
import com.sergeynv.holidays.data.YearHolidays
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_EITHER
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

internal class HolidaysViewModel : ViewModel() {
    private val holidaysRepository = HolidaysRepository()

    val countries: LiveData<List<Country>>
    val isFetchingCountries: LiveData<Boolean>

    // "Free accounts are limited to last year's historical data only. Upgrade to premium for
    // access to all holiday data." (c) HolidaysApi.com
    // The "initial" value is the only year our API will serve data for.
    var year: Int? = currentYear - 1
        @MainThread set(value) {
            if (field == value) return
            field = value
            maybeRefetchHolidays()
        }

    var countryA: Country? = null
        @MainThread set(value) {
            if (field == value) return
            field = value
            maybeRefetchHolidays()
        }

    var countryB: Country? = null
        @MainThread set(value) {
            if (field == value) return
            field = value
            maybeRefetchHolidays()
        }

    var selectedHolidaysFilterStrategy = IN_EITHER

    private var holidaysFetchingJob: Job? = null

    private val _holidays = MutableLiveData<YearHolidays?>(null)
    val holidays: LiveData<YearHolidays?> = _holidays

    private val _isFetchingHolidays = MutableLiveData(false)
    val isFetchingHolidays: LiveData<Boolean> = _isFetchingHolidays

    private val scope by lazy {
        viewModelScope + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Exception in a coroutine on ${Thread.currentThread()}: \n$throwable")
            showToast(throwable.message ?: "Unknown error")
        }
    }

    init {
        val countriesMutable = MutableLiveData<List<Country>>()
        countries = countriesMutable
        val isFetchingCountriesMutable = MutableLiveData(false)
        isFetchingCountries = isFetchingCountriesMutable

        scope.launch {
            isFetchingCountriesMutable.value = true
            try {
                countriesMutable.value = holidaysRepository.getCountries()
            } finally {
                isFetchingCountriesMutable.value = false
            }
        }
    }

    @MainThread
    private fun maybeRefetchHolidays() {
        holidaysFetchingJob?.cancel()?.also {
            holidaysFetchingJob = null
        }
        _holidays.value = null
        _isFetchingHolidays.value = false

        // Check if we have enough input to start fetching.
        if (year == null || (countryA == null && countryB == null)) return

        scope.launch {
            _isFetchingHolidays.value = true
            try {
                _holidays.value = holidaysRepository.getHolidays(year!!, countryA, countryB)
            } finally {
                _isFetchingHolidays.value = false
            }
        }
    }

    private fun showToast(message: String) =
        Toast.makeText(HolidaysApplication.instance, message, Toast.LENGTH_SHORT).show()

    companion object {
        private const val TAG = "HolidaysViewModel"
    }
}
