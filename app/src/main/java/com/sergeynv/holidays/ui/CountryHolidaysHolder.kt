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

internal class CountryHolidaysHolder(
    private val scope: CoroutineScope,
    private val holidaysService: HolidaysService
) {
    var country: Country? = null
        @MainThread set(value) {
            field = value
            clearAndMaybeRefetchHolidays()
        }
    var year: Int? = null
        @MainThread set(value) {
            field = value
            clearAndMaybeRefetchHolidays()
        }

    private val _holidays = MutableLiveData<List<Holiday>?>(null)
    var holidays: LiveData<List<Holiday>?> = _holidays

    private val _isFetching = MutableLiveData(false)
    val isFetching: LiveData<Boolean> = _isFetching

    private var fetchingJob: Job? = null

    @MainThread
    private fun clearAndMaybeRefetchHolidays() {
        clear()
        if (country != null && year != null) {
            fetchHolidays(country!!, year!!)
        }
    }

    /**
     * Remove stored [holidays] (if any), cancel on-going [fetchingJob] (if any) and start
     * fetching holidays in the given [country] in the given [year].
     */
    @MainThread
    private fun fetchHolidays(country: Country, year: Int) {
        fetchingJob = scope.launch {
            log("getHolidays(${country.code}, ${year}) starting...")
            _isFetching.value = true
            try {
                _holidays.value = holidaysService.getHolidays(country.code, year)
                    .also { log("getHolidays(${country.code}, $year) finished: $it") }
                    .holidays
            } finally {
                _isFetching.value = false
            }
        }
    }

    @MainThread
    private fun clear() {
        fetchingJob?.cancel()?.also {
            fetchingJob = null
        }
        _holidays.value = null
        _isFetching.value = false
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