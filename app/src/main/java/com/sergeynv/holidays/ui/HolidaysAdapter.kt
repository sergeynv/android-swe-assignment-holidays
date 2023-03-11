package com.sergeynv.holidays.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sergeynv.holidays.data.Holiday
import java.text.SimpleDateFormat
import java.util.Locale

class HolidaysAdapter : RecyclerView.Adapter<HolidaysAdapter.ViewHolder>() {
    var holidays: List<Holiday> = emptyList()
        set(value) {
            field = value.toList()
            notifyDataSetChanged()
        }

    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
            .let { ViewHolder(it) }

    override fun getItemCount(): Int = holidays.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holidays[position].apply {
            holder.textView.text = "${dateFormat.format(date)} - ${name}"
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    companion object {
        private const val DATE_FORMAT = "EEE, MMM d" // Wed, Jul 4
    }
}