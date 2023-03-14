package com.sergeynv.holidays.data

interface HolidaysRepository {
    suspend fun getCountries(): List<Country>
    suspend fun getHolidays(year: Int, countryA: Country, countryB: Country? = null): YearHolidays
}