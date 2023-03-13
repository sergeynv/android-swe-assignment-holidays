package com.sergeynv.holidays.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.sergeynv.holidays.R
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
            .inflate(R.layout.spinner_item_country, parent, false)
        return ViewHolder(view)
    }

    private fun bindViewHolder(position: Int, viewHolder: ViewHolder) = viewHolder.apply {
        val country = getItem(position)
        nameLabel.text = country?.name ?: "-"
        flagIcon.visibility = if (country != null) View.VISIBLE else View.INVISIBLE
        // TODO: start loading flag
    }

    private class ViewHolder(val view: View) {
        val flagIcon: ImageView = view.findViewById(R.id.imageView_flag)
        val nameLabel: TextView = view.findViewById(R.id.textView_name)
    }
}