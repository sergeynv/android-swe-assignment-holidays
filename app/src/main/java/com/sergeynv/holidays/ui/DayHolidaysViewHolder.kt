package com.sergeynv.holidays.ui

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.data.DayHolidays
import com.sergeynv.holidays.data.Holiday
import java.text.SimpleDateFormat
import java.util.Locale

internal class DayHolidaysViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val date: TextView = view.findViewById(R.id.date)
    private val weekday: TextView = view.findViewById(R.id.weekday)
    private val holidayRow1 = HolidayRowViewHolder(view.findViewById(R.id.holiday1))
    private val holidayRow2 = HolidayRowViewHolder(view.findViewById(R.id.holiday2))
    private val moreHolidays: TextView = view.findViewById(R.id.more_holidays)

    internal fun bind(dayHolidays: DayHolidays, countryA: Country?, countryB: Country?) =
        with(dayHolidays) {
            // Sanity checks.
            require(inA != null || inB != null)
            require(inA == null || countryA != null && inA.isNotEmpty())
            require(inB == null || countryB != null && inB.isNotEmpty())

            // Display the date.
            this@DayHolidaysViewHolder.date.text = dateFormat.format(date)
            this@DayHolidaysViewHolder.weekday.text = weekdayFormat.format(date)

            // Display the country codes.
            if (isInBoth) {
                holidayRow1.bind(countryA!!, inA!!)
                holidayRow2.apply { isVisible = true }.bind(countryB!!, inB!!)
            } else {
                val (country, holidays) = if (inA != null) countryA!! to inA else countryB!! to inB!!
                holidayRow1.bind(country, holidays)
                holidayRow2.isVisible = false // visibility = GONE
            }

            // At the moment we show at most 1 holiday per country, hence the following:
            val moreHolidays = (inA?.let { it.size - 1 } ?: 0) + (inB?.let { it.size - 1 } ?: 0)
            this@DayHolidaysViewHolder.moreHolidays.apply {
                if (moreHolidays > 0) {
                    isVisible = true
                    text = "and $moreHolidays more"
                } else {
                    isVisible = false
                }
            }
        }

    private fun canCombine(inA: List<Holiday>, inB: List<Holiday>) =
        inA.first().name == inB.first().name && inA.size == inB.size

    companion object {
        private val dateFormat by lazy { SimpleDateFormat("MMM d", Locale.getDefault()) }
        private val weekdayFormat by lazy { SimpleDateFormat("EEEE", Locale.getDefault()) }
    }

    private class HolidayRowViewHolder(private val container: View) {
        private val holidayName: TextView = container.findViewById(R.id.name)
        private val countryCode: TextView = container.findViewById(R.id.country_code)
        private val details: TextView = container.findViewById(R.id.details)

        fun bind(country: Country, holidays: List<Holiday>) {
            countryCode.text = country.code
            holidayName.text = holidays.first().name
        }

        inline var isVisible: Boolean
            get() = container.isVisible
            set(value) { container.isVisible = value }
    }
}
