package com.sergeynv.holidays.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sergeynv.holidays.api.HolidaysService
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.utils.currentYear
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

internal class HolidaysViewModel(application: Application) : AndroidViewModel(application) {
    val countries: LiveData<List<Country>>
    val isFetchingCountries: LiveData<Boolean>

    val holidaysHolderA: CountryHolidaysHolder
    val holidaysHolderB: CountryHolidaysHolder

    var selectedHolidaysFilterStrategy = HolidaysFilterStrategy.IN_EITHER

    private val scope by lazy {
        viewModelScope + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Exception in a coroutine on ${Thread.currentThread()}: \n$throwable")
            showToast(throwable.message ?: "Unknown error")
        }
    }

    init {
        val holidaysService = HolidaysService.retrofit

        val _countries = MutableLiveData<List<Country>>()
        countries = _countries
        val _isFetchingCountries = MutableLiveData(false)
        isFetchingCountries = _isFetchingCountries
        scope.launch {
            Log.d(TAG, "Fetching country list...")
            _isFetchingCountries.value = true
            try {
                _countries.value = holidaysService.getCountries()
                    .also { Log.d(TAG, "getCountries() finished: $it") }
                    .countries
                    .let { list -> withContext(Dispatchers.Default) { list.sortedBy { it.name } } }
            } finally {
                _isFetchingCountries.value = false
            }
        }

        // "Free accounts are limited to last year's historical data only.
        // Upgrade to premium for access to all holiday data."
        // (c) HolidaysApi.com
        // The "initial" value is the only year our API will serve data for.
        val initialYear = currentYear - 1
        holidaysHolderA = CountryHolidaysHolder(scope, holidaysService)
            .apply { year = initialYear }
        holidaysHolderB = CountryHolidaysHolder(scope, holidaysService)
            .apply { year = initialYear }
    }

    private fun showToast(message: String) =
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()

    companion object {
        private const val TAG = "HolidaysViewModel"
    }
}
