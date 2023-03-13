package com.sergeynv.holidays.ui

import com.sergeynv.holidays.data.Holiday
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Note: there may be more than 1 holiday on the same day.
 * For example: in Germany on May 26th, 2022 it's the "Father's Day" and
 * the "Feast of the Ascension of Jesus Christ".
 * https://holidayapi.com/v1/holidays?country=DE&year=2022&key=8780ae23-9cdd-4616-8b4e-c2b3107c2cdd&pretty
 */
internal data class DayHolidays(
    val date: Date,
    val inA: List<Holiday>?, // Either null or not(!) empty
    val inB: List<Holiday>?  // Either null or not(!) empty
) {
    init {
        inA?.let { require(it.isNotEmpty()) }
        inB?.let { require(it.isNotEmpty()) }
        require(inA != null || inB != null) {
            "inA and inB must NOT be null at the same time"
        }
    }

    val isInBoth: Boolean = inA != null && inB != null
    val isOnlyInA: Boolean = inA != null && inB == null
    val isOnlyInB: Boolean = inA == null && inB != null

    override fun toString(): String = """
        ${dateFormat.format(date)}
           in A: $inA
           in B: $inB
        """.trimIndent()

    companion object {
        private val dateFormat by lazy {
            SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        }
    }
}