package com.sergeynv.holidays.data

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ListHolidaysResponse(
    val status: Int,
    val holidays: List<Holiday>
)
