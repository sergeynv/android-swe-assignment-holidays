package com.sergeynv.holidays.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.sergeynv.holidays.HolidaysApplication
import com.sergeynv.holidays.api.HolidaysService
import com.sergeynv.holidays.di.Dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.Date

private typealias HolidaysListsPair = Pair<MutableList<Holiday>, MutableList<Holiday>>

private val HolidaysListsPair.holidaysInA
    get() = first
private val HolidaysListsPair.holidaysInAorNull
    get() = holidaysInA.takeUnless { it.isEmpty() }

private val HolidaysListsPair.holidaysInB
    get() = second
private val HolidaysListsPair.holidaysInBorNull
    get() = holidaysInB.takeUnless { it.isEmpty() }

private typealias DateToHolidaysMap = MutableMap<Date, HolidaysListsPair>

private fun DateToHolidaysMap.holidaysInA(on: Date) =
    getOrPut(on) { (mutableListOf<Holiday>() to mutableListOf()) }.holidaysInA

private fun DateToHolidaysMap.holidaysInB(on: Date) =
    getOrPut(on) { (mutableListOf<Holiday>() to mutableListOf()) }.holidaysInB

class HolidaysRepository(
    private val holidaysService: HolidaysService = Dependencies.holidaysService
) {
    suspend fun getCountries(): List<Country> = holidaysService
        .debug { "Fetching countries..." }
        .getCountries().countries
        .debug { "Fetched ${it.size} countries" }

    suspend fun getHolidays(
        year: Int,
        countryA: Country? = null,
        countryB: Country? = null
    ): YearHolidays = coroutineScope {
        require(countryA != null || countryB != null)

        val fetchingA = countryA?.let { fetchHolidaysAsync(year, it) }
        val fetchingB = countryB?.let { fetchHolidaysAsync(year, it) }

        val holidaysInA = fetchingA?.await()
        val holidaysInB = fetchingB?.await()

        // We are going to do some merging and sorting, which should be all that CPU intensive, but
        // just to be super safe let's make sure we are not doing this on the UI thread.
        withContext(Dispatchers.Default) {
            val dateToHolidaysMap: DateToHolidaysMap = mutableMapOf()
            holidaysInA?.forEach { dateToHolidaysMap.holidaysInA(on = it.date).add(it) }
            holidaysInB?.forEach { dateToHolidaysMap.holidaysInB(on = it.date).add(it) }

            val holidaysList = dateToHolidaysMap.map {
                DayHolidays(
                    date = it.key,
                    inA = it.value.holidaysInAorNull,
                    inB = it.value.holidaysInBorNull
                )
            }.sortedBy { it.date }

            YearHolidays(year, countryA, countryB, holidaysList)
        }
    }

    private fun CoroutineScope.fetchHolidaysAsync(year: Int, country: Country) = async {
        holidaysService
            .debug { "Fetching holidays in ${country.code} in $year..." }
            .getHolidays(year, country.code).holidays
            .debug { "Fetched ${it.size} holidays in ${country.code} in $year" }
    }

    companion object {
        private const val TAG = "HolidaysRepository"
        private const val ENABLE_DEBUG_TOASTS = true

        private val appContext: Context by lazy { HolidaysApplication.instance }

        private fun <T> T.debug(messageProducer: (T) -> String): T = messageProducer(this).let {
            Log.d(TAG, it)
            if (ENABLE_DEBUG_TOASTS) {
                Toast.makeText(appContext, it, Toast.LENGTH_SHORT).show()
            }
            this
        }
    }
}
