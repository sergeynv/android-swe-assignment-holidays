package com.sergeynv.holidays.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergeynv.holidays.api.HolidaysService
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.utils.currentYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HolidaysViewModel : ViewModel() {
    val countries: LiveData<List<Country>>
    val isFetchingCountries: LiveData<Boolean>

    val holidaysHolderA: CountryHolidaysHolder
    val holidaysHolderB: CountryHolidaysHolder

    var selectedHolidaysFilterStrategy = HolidaysFilterStrategy.IN_EITHER

    init {
        val holidaysService = HolidaysService.retrofit

        val _countries = MutableLiveData<List<Country>>()
        countries = _countries
        val _isFetchingCountries = MutableLiveData(false)
        isFetchingCountries = _isFetchingCountries
        viewModelScope.launch {
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
        holidaysHolderA = CountryHolidaysHolder(viewModelScope, holidaysService)
            .apply { year = initialYear }
        holidaysHolderB = CountryHolidaysHolder(viewModelScope, holidaysService)
            .apply { year = initialYear }
    }

    companion object {
        private const val TAG = "HolidaysViewModel"
    }
}
