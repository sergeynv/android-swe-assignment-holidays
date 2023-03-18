package com.sergeynv.holidays.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.R
import com.sergeynv.holidays.data.DayHolidays
import com.sergeynv.holidays.data.YearHolidays
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_A_NOT_IN_B
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_BOTH
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_B_NOT_IN_A
import com.sergeynv.holidays.ui.HolidaysFilterStrategy.IN_EITHER
import java.lang.RuntimeException

internal class HolidaysAdapter: RecyclerView.Adapter<DayHolidaysViewHolder>() {
    var yearHolidays: YearHolidays? = null
        set(value) {
            field = value
            maybeFilterAndUpdate()
        }
    private var filteredHolidays: List<DayHolidays>? = null

    // We only take this into account if we have holidays for both(!) countries.
    // If we have holidays for only one of the two counties, this is ignored, and we simply show all
    // holidays.
    var filterStrategy = IN_EITHER
        set(value) {
            field = value
            maybeFilterAndUpdate()
        }

    private val countryA
        get() = yearHolidays?.countryA
    private val countryB
        get() = yearHolidays?.countryB

    private fun maybeFilterAndUpdate() {
        Log.d(TAG, "maybeFilterAndUpdate()")

        val allHolidays = yearHolidays?.holidays // Makes compiler happier below.

        if (yearHolidays == null && filteredHolidays == null) {
            // Have nothing to show now and had nothing to show before
            return
        } else if (yearHolidays == null) {
            // Simply remove everything.
            // Be sure to get item count before "unsetting" filteredHolidays.
            itemCount
                .also { filteredHolidays = null }
                .let { notifyItemRangeRemoved(0, it) }
            return
        }

        if (countryA == null || countryB == null || filterStrategy == IN_EITHER) {
            // We do not filter unless we have both countries. We also do not need to do any
            // filtering if "Show holidays in either A or B selected".
            filteredHolidays = allHolidays
        } else {
            filteredHolidays = allHolidays?.filter {
                when(filterStrategy) {
                    IN_BOTH -> it.isInBoth
                    IN_A_NOT_IN_B -> it.isOnlyInA
                    IN_B_NOT_IN_A -> it.isOnlyInB
                    IN_EITHER -> throw RuntimeException("This is unreachable") // we handled this above.
                }
            }
        }

        // TODO: do a proper diffing instead of the "blanket" notify, if what's that changed is the
        //  filtering strategy.
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_day_holidays, parent, false)
            .let { DayHolidaysViewHolder(it) }

    override fun getItemCount(): Int = filteredHolidays?.size ?: 0

    override fun onBindViewHolder(holder: DayHolidaysViewHolder, position: Int) {
        holder.bind(dayHolidays = filteredHolidays!![position], countryA, countryB)
    }

    companion object {
        private const val TAG = "HolidaysAdapter"
    }
}