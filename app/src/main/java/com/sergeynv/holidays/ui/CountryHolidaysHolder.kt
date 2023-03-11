package com.sergeynv.holidays.ui

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergeynv.holidays.api.HolidaysService
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.data.Holiday
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CountryHolidaysHolder(
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
            log("getHolidays(${country.code}, ${year}) starting...")
            _isFetching.value = true
            try {
                holidays = holidaysService.getHolidays(country.code, year)
                    .also { log("getHolidays(${country.code}, $year) finished: $it") }
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

    companion object {
        private const val TAG = "CountryHolidaysHolder"
        private const val LOG_THREAD = true

        private fun log(message: String) = if (LOG_THREAD) {
            Log.d(TAG, "[${Thread.currentThread().toShortString()}] $message")
        } else {
            Log.d(TAG, message)
        }

        private fun Thread.toShortString() = "$name-$id"
    }
}