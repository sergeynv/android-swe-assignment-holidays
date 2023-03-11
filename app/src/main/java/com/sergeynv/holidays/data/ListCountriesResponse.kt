package com.sergeynv.holidays.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListCountriesResponse(
    val status: Int,
    val countries: List<Country>
)
