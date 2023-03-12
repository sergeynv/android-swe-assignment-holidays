package com.sergeynv.holidays.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.data.Holiday
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HolidaysAdapter(
    private val holidaysHolderA: CountryHolidaysHolder,
    private val holidaysHolderB: CountryHolidaysHolder,
    lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<HolidaysAdapter.ViewHolder>() {
    private var allHolidays: List<HolidayHolder>? = null

    // We only take this into account if we have holidays for both(!) countries.
    // If we have holidays for only one of the two counties, this is ignored, and we simply show all
    // holidays.
    var filterStrategy = HolidaysFilterStrategy.IN_EITHER
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    init {
        holidaysHolderA.holidays.observe(lifecycleOwner) { recompute() }
        holidaysHolderB.holidays.observe(lifecycleOwner) { recompute() }
    }

    private fun recompute() {
        Log.d(TAG, "recompute()")

        val map: MutableMap<Date, Pair<MutableList<Holiday>, MutableList<Holiday>>> = mutableMapOf()
        holidaysHolderA.holidays.value
            .also { Log.d(TAG, "  a: $it") }
            ?.forEach { map.getOrPut(it.date).first.add(it) }
        holidaysHolderB.holidays.value
            .also { Log.d(TAG, "  b: $it") }
            ?.forEach { map.getOrPut(it.date).second.add(it) }

        allHolidays = map.map {
            HolidayHolder(
                date = it.key,
                inA = it.value.first.takeUnless { holidays -> holidays.isEmpty() },
                inB = it.value.second.takeUnless { holidays -> holidays.isEmpty() },
            )
        }
            .sortedBy { it.date }
            .also { Log.d(TAG, "  all:\n${it.joinToString("\n")}") }
            .takeUnless { it.isEmpty() }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_holiday, parent, false)
            .let { ViewHolder(it) }

    override fun getItemCount(): Int = allHolidays?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(allHolidays!![position], holidaysHolderA.country, holidaysHolderB.country)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
            holidayHolder: HolidayHolder,
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
                countryCodeB.visibility = VISIBLE
                countryCodeA.text = countryA!!.code
                countryCodeB.text = countryB!!.code
            } else {
                countryCodeB.visibility = GONE
                countryCodeA.text = (if (inA != null) countryA else countryB)!!.code
            }

            // Display actual holidays
            if (inOneCountry || canCombine(inA!!, inB!!)) {
                val holidays = inA ?: inB!!
                holidayInAName.text = holidays.first().name
                holidayInAMore.apply {
                    if (holidays.size > 1) {
                        visibility = VISIBLE
                        text = "and ${holidays.size - 1} more"
                    } else {
                        visibility = GONE
                    }
                }

                holidayInBContainer.visibility = GONE
            } else {
                holidayInAName.text = inA.first().name
                holidayInAMore.apply {
                    if (inA.size > 1) {
                        visibility = VISIBLE
                        text = "and ${inA.size - 1} more"
                    } else {
                        visibility = GONE
                    }
                }

                holidayInBContainer.visibility = VISIBLE
                holidayInBName.text = inA.first().name
                holidayInBMore.apply {
                    if (inB.size > 1) {
                        visibility = VISIBLE
                        text = "and ${inB.size - 1} more"
                    } else {
                        visibility = GONE
                    }
                }
            }
        }

        private fun canCombine(inA: List<Holiday>, inB: List<Holiday>) =
            inA.first().name == inB.first().name && inA.size == inB.size
    }

    /**
     * Note: there may be more than 1 holiday on the same day.
     * For example: in Germany on May 26th, 2022 it's the "Father's Day" and
     * the "Feast of the Ascension of Jesus Christ".
     * https://holidayapi.com/v1/holidays?country=DE&year=2022&key=8780ae23-9cdd-4616-8b4e-c2b3107c2cdd&pretty
     */
    internal data class HolidayHolder(
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

        val inOneCountry: Boolean = inA == null || inB == null
        val inBothCountries: Boolean = inA != null && inB != null

        override fun toString(): String = """
            ${dateFormat.format(date)}
               in A: $inA
               in B: $inB
        """.trimIndent()
    }

    companion object {
        private const val TAG = "HolidaysAdapter"

        private const val DATE_FORMAT_PATTERN = "EEE, MMM d" // Wed, Jul 4
        private val dateFormat by lazy {
            SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
        }

        private val monthDayFormat by lazy { SimpleDateFormat("MMM d", Locale.getDefault()) }
        private val weekdayFormat by lazy { SimpleDateFormat("EEEE", Locale.getDefault()) }

        private fun MutableMap<Date, Pair<MutableList<Holiday>, MutableList<Holiday>>>.getOrPut(
            date: Date
        ) = getOrPut(date) { (mutableListOf<Holiday>() to mutableListOf()) }

        private fun List<Holiday>.toShortString(): String = StringBuilder(first().name)
            .also { if (size > 1) it.append(" +${size - 1} more") }
            .toString()
    }
}