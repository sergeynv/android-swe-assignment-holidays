package com.sergeynv.holidays.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.net.URL

@JsonClass(generateAdapter = true)
data class Country(
    val name: String,
    val code: String,
    @Json(name = "flag") val flagUrl: URL
)
