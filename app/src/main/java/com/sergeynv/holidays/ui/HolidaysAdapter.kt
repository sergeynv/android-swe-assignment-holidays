package com.sergeynv.holidays.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.data.Holiday
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HolidaysAdapter(
    private val holidaysHolderA: CountryHolidaysHolder,
    private val holidaysHolderB: CountryHolidaysHolder,
    lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<HolidaysAdapter.ViewHolder>() {
    private var allHolidays: List<HolidayHolder>? = null

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
            .inflate(android.R.layout.simple_list_item_1, parent, false)
            .let { ViewHolder(it) }

    override fun getItemCount(): Int = allHolidays?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        allHolidays?.let {
            it[position].apply {
                holder.textView.text = """
                    ${dateFormat.format(date)}
                      ${inA?.toShortString() ?: "-"}
                      ${inB?.toShortString() ?: "-"}
                    """.trimIndent()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    /**
     * Note: there may be more than 1 holiday on the same day.
     * For example: in Germany on May 26th, 2022 it's the "Father's Day" and
     * the "Feast of the Ascension of Jesus Christ".
     * https://holidayapi.com/v1/holidays?country=DE&year=2022&key=8780ae23-9cdd-4616-8b4e-c2b3107c2cdd&pretty
     */
    private data class HolidayHolder(
        val date: Date,
        val inA: List<Holiday>?,
        val inB: List<Holiday>?
    ) {
        init {
            require(inA != null || inB != null) {
                "inA and inB must NOT be null at the same time"
            }
        }

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

        private fun MutableMap<Date, Pair<MutableList<Holiday>, MutableList<Holiday>>>.getOrPut(
            date: Date
        ) = getOrPut(date) { (mutableListOf<Holiday>() to mutableListOf()) }

        private fun List<Holiday>.toShortString(): String = StringBuilder(first().name)
            .also { if (size > 1) it.append(" +${size - 1} more") }
            .toString()
    }
}