package com.sergeynv.holidays.api

import com.sergeynv.holidays.data.Country
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListCountriesResponse(
    val status: Int,
    val countries: List<Country>
)
