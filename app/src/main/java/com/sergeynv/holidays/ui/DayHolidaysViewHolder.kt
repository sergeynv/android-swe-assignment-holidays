package com.sergeynv.holidays.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.data.Holiday
import java.text.SimpleDateFormat
import java.util.Locale

internal class DayHolidaysViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val monthDay: TextView = view.findViewById(R.id.date_month_day)
    private val weekday: TextView = view.findViewById(R.id.date_weekday)
    private val countryCodeA: TextView = view.findViewById(R.id.country_code_a)
    private val countryCodeB: TextView = view.findViewById(R.id.country_code_b)
    private val holidayInAContainer: View = view.findViewById(R.id.a_holidays_container)
    private val holidayInAName: TextView = holidayInAContainer.findViewById(R.id.a_holiday_name)
    private val holidayInAMore: TextView =
        holidayInAContainer.findViewById(R.id.a_more_holidays)
    private val holidayInBContainer: View = view.findViewById(R.id.b_holidays_container)
    private val holidayInBName: TextView = holidayInBContainer.findViewById(R.id.b_holiday_name)
    private val holidayInBMore: TextView =
        holidayInBContainer.findViewById(R.id.b_more_holidays)

    internal fun bind(
        holidayHolder: DayHolidaysHolder,
        countryA: Country?,
        countryB: Country?
    ) = with(holidayHolder) {
        // Sanity checks.
        require(inA != null || inB != null)
        require(inA == null || countryA != null && inA.isNotEmpty())
        require(inB == null || countryB != null && inB.isNotEmpty())

        // Display the date.
        monthDay.text = monthDayFormat.format(date)
        weekday.text = weekdayFormat.format(date)

        // Display the country codes.
        if (inBothCountries) {
            countryCodeB.visibility = View.VISIBLE
            countryCodeA.text = countryA!!.code
            countryCodeB.text = countryB!!.code
        } else {
            countryCodeB.visibility = View.GONE
            countryCodeA.text = (if (inA != null) countryA else countryB)!!.code
        }

        // Display actual holidays
        if (inOneCountry || canCombine(inA!!, inB!!)) {
            val holidays = inA ?: inB!!
            holidayInAName.text = holidays.first().name
            holidayInAMore.apply {
                if (holidays.size > 1) {
                    visibility = View.VISIBLE
                    text = "and ${holidays.size - 1} more"
                } else {
                    visibility = View.GONE
                }
            }

            holidayInBContainer.visibility = View.GONE
        } else {
            holidayInAName.text = inA.first().name
            holidayInAMore.apply {
                if (inA.size > 1) {
                    visibility = View.VISIBLE
                    text = "and ${inA.size - 1} more"
                } else {
                    visibility = View.GONE
                }
            }

            holidayInBContainer.visibility = View.VISIBLE
            holidayInBName.text = inA.first().name
            holidayInBMore.apply {
                if (inB.size > 1) {
                    visibility = View.VISIBLE
                    text = "and ${inB.size - 1} more"
                } else {
                    visibility = View.GONE
                }
            }
        }
    }

    private fun canCombine(inA: List<Holiday>, inB: List<Holiday>) =
        inA.first().name == inB.first().name && inA.size == inB.size

    companion object {
        private val monthDayFormat by lazy { SimpleDateFormat("MMM d", Locale.getDefault()) }
        private val weekdayFormat by lazy { SimpleDateFormat("EEEE", Locale.getDefault()) }
    }
}
