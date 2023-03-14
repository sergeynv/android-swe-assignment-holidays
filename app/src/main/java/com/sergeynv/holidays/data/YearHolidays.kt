package com.sergeynv.holidays.data

import java.lang.IllegalStateException

data class YearHolidays(
    val year: Int,
    val countryA: Country?,
    val countryB: Country?,
    val holidays: List<DayHolidays>
) {
    val isOneCountry: Boolean
        get() = countryA == null || countryB == null

    val country: Country
        get() = if (isOneCountry) countryA ?: countryB!! else
            throw IllegalStateException("This method should NOT be used if both countries are set")

    init {
        require(countryA != null || countryB != null) {
            "Both countries A and B MUST NOT be null at the same time"
        }
    }
}