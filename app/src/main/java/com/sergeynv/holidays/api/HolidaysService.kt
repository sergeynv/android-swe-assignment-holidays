package com.sergeynv.holidays.api

import com.sergeynv.holidays.data.ListCountriesResponse
import com.sergeynv.holidays.data.ListHolidaysResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface HolidaysService {
    @GET("countries")
    suspend fun getCountries(): ListCountriesResponse

    @GET("countries")
    suspend fun searchCountries(
        @Query("search") search: String
    ): ListCountriesResponse

    @GET("holidays")
    suspend fun getHolidays(
        @Query("country") countryCode: String,
        @Query("year") year: Int,
    ): ListHolidaysResponse
}