package com.sergeynv.holidays.api

import com.sergeynv.holidays.data.Holiday
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ListHolidaysResponse(
    val status: Int,
    val holidays: List<Holiday>
)
