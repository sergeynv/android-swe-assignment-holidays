package com.sergeynv.holidays.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.Country
import com.sergeynv.holidays.data.Holiday
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_A_NOT_IN_B
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_BOTH
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_B_NOT_IN_A
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_EITHER
import java.lang.RuntimeException
import java.util.Date

internal class HolidaysAdapter(
    private val holidaysHolderA: CountryHolidaysHolder,
    private val holidaysHolderB: CountryHolidaysHolder,
    lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<DayHolidaysViewHolder>() {
    private var allHolidays: List<DayHolidays>? = null
    private var filteredHolidays: List<DayHolidays>? = null

    // We only take this into account if we have holidays for both(!) countries.
    // If we have holidays for only one of the two counties, this is ignored, and we simply show all
    // holidays.
    var filterStrategy = IN_EITHER
        set(value) {
            field = value
            filter()
            notifyDataSetChanged()
        }

    init {
        fun fullRecompute() {
            compute()
            filter()
            notifyDataSetChanged()
        }
        holidaysHolderA.holidays.observe(lifecycleOwner) { fullRecompute() }
        holidaysHolderB.holidays.observe(lifecycleOwner) { fullRecompute() }
    }

    private val countryA: Country?
        get() = holidaysHolderA.country
    private val countryB
        get() = holidaysHolderB.country

    private val holidaysInA
        get() = holidaysHolderA.holidays.value
    private val holidaysInB
        get() = holidaysHolderB.holidays.value

    private fun compute() {
        Log.d(TAG, "compute()")

        val dateToHolidaysMap =
            mutableMapOf<Date, Pair<MutableList<Holiday>, MutableList<Holiday>>>()
                .apply {
                    holidaysInA?.forEach { getOrPut(it.date).first.add(it) }
                    holidaysInB?.forEach { getOrPut(it.date).second.add(it) }
                }

        allHolidays = dateToHolidaysMap.map {
            DayHolidays(
                date = it.key,
                inA = it.value.first.takeUnless { holidays -> holidays.isEmpty() },
                inB = it.value.second.takeUnless { holidays -> holidays.isEmpty() },
            )
        }.sortedBy { it.date }
            .also { Log.d(TAG, "  all:\n${it.joinToString("\n")}") }
            .takeUnless { it.isEmpty() }
    }

    private fun filter() {
        Log.d(TAG, "filter()")

        if (holidaysInA == null || holidaysInB == null || filterStrategy == IN_EITHER) {
            // We do not filter unless we have holidays for both countries!
            filteredHolidays = allHolidays
            return
        }

        filteredHolidays = allHolidays?.filter {
            when(filterStrategy) {
                IN_BOTH -> it.isInBoth
                IN_A_NOT_IN_B -> it.isOnlyInA
                IN_B_NOT_IN_A -> it.isOnlyInB
                IN_EITHER -> throw RuntimeException("This is unreachable") // we handled this above.
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_holiday, parent, false)
            .let { DayHolidaysViewHolder(it) }

    override fun getItemCount(): Int = filteredHolidays?.size ?: 0

    override fun onBindViewHolder(holder: DayHolidaysViewHolder, position: Int) {
        holder.bind(holidayHolder = filteredHolidays!![position], countryA, countryB)
    }

    companion object {
        private const val TAG = "HolidaysAdapter"

        private fun MutableMap<Date, Pair<MutableList<Holiday>, MutableList<Holiday>>>.getOrPut(
            date: Date
        ) = getOrPut(date) { (mutableListOf<Holiday>() to mutableListOf()) }
    }
}