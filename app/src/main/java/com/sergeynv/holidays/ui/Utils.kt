package com.sergeynv.holidays.ui

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import java.util.Calendar

internal val currentYear = Calendar.getInstance().get(Calendar.YEAR)

internal fun Spinner.onItemSelected(block: (Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) = block(position)

        override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */ }
    }
}