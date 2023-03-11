package com.sergeynv.holidays.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.data.Holiday
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HolidaysAdapter(
    private val holidaysHolderA: CountryHolidaysHolder,
    private val holidaysHolderB: CountryHolidaysHolder,
    lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<HolidaysAdapter.ViewHolder>() {
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private var allHolidays: List<HolidayHolder>? = null

    init {
        holidaysHolderA.holidays.observe(lifecycleOwner) { recompute() }
        holidaysHolderB.holidays.observe(lifecycleOwner) { recompute() }
    }

    private fun recompute() {
        val map: MutableMap<Date, Pair<Holiday?, Holiday?>> = mutableMapOf()
        holidaysHolderA.holidays.value?.forEach {
            map[it.date] = (it to null)
        }
        holidaysHolderB.holidays.value?.forEach {
            map[it.date] = map[it.date]?.copy(second = it) ?: (null to it)
        }

        allHolidays = map.map {
            HolidayHolder(
                date = it.key,
                inA = it.value.first,
                inB = it.value.second)
        }.takeUnless { it.isEmpty() }?.sortedBy { it.date }

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
                holder.textView.text = "${dateFormat.format(date)} - ${inA?.name} - ${inB?.name}"
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    private data class HolidayHolder(
        val date: Date,
        val inA: Holiday?,
        val inB: Holiday?
    ) {
        init {
            require(inA != null || inB != null) {
                "inA and inB must NOT be null at the same time"
            }
        }
    }

    companion object {
        private const val DATE_FORMAT = "EEE, MMM d" // Wed, Jul 4
    }
}