package com.sergeynv.holidays.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.sergeynv.holidays.data.Country
import java.lang.RuntimeException

internal class CountryListSpinnerAdapter(private val countries: List<Country>) : BaseAdapter() {
    override fun getCount(): Int = countries.size + 1

    override fun getItem(position: Int): Country? =
        if (position > 0) countries[position - 1] else null

    override fun getItemId(position: Int): Long = getItem(position)?.hashCode()?.toLong() ?: 0L

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder = convertView?.tag as ViewHolder? ?: createViewHolder(parent!!)
        bindViewHolder(position, viewHolder)
        return viewHolder.view
    }

    fun getItemPosition(item: Country?): Int {
        if (item == null) return 0
        val listIndex = countries.indexOf(item)
        if (listIndex < 0) throw RuntimeException()
        return listIndex + 1
    }

    private fun createViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_spinner_item, parent, false)
        return ViewHolder(view)
    }

    private fun bindViewHolder(position: Int, viewHolder: ViewHolder) {
        viewHolder.textView.text = getItem(position)?.name ?: "-"
    }

    private class ViewHolder(val view: View) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }
}