package com.sergeynv.holidays.api

import retrofit2.http.GET
import retrofit2.http.Query

interface HolidaysService {
    @GET("countries")
    suspend fun getCountries(): ListCountriesResponse

    @GET("holidays")
    suspend fun getHolidays(
        @Query("year") year: Int,
        @Query("country") countryCode: String,
    ): ListHolidaysResponse
}