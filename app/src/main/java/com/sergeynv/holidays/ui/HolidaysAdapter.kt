package com.sergeynv.holidays.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.Holiday
import java.util.Date

internal class HolidaysAdapter(
    private val holidaysHolderA: CountryHolidaysHolder,
    private val holidaysHolderB: CountryHolidaysHolder,
    lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<DayHolidaysViewHolder>() {
    private var allHolidays: List<DayHolidaysHolder>? = null

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
            DayHolidaysHolder(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_holiday, parent, false)
            .let { DayHolidaysViewHolder(it) }

    override fun getItemCount(): Int = allHolidays?.size ?: 0

    override fun onBindViewHolder(holder: DayHolidaysViewHolder, position: Int) {
        holder.bind(
            holidayHolder = allHolidays!![position],
            countryA = holidaysHolderA.country,
            countryB = holidaysHolderB.country
        )
    }

    companion object {
        private const val TAG = "HolidaysAdapter"

        private fun MutableMap<Date, Pair<MutableList<Holiday>, MutableList<Holiday>>>.getOrPut(
            date: Date
        ) = getOrPut(date) { (mutableListOf<Holiday>() to mutableListOf()) }
    }
}