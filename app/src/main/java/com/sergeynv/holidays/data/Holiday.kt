package com.sergeynv.holidays.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class Holiday(
    val name: String,
    @Json(name = "public") val isPublic: Boolean,
    val date: Date,
    @Json(name = "observed") val observedOn: Date
)