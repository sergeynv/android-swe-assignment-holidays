package com.sergeynv.holidays.ui

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal val currentYear = Calendar.getInstance().get(Calendar.YEAR)

internal fun Spinner.onItemSelected(block: (Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) = block(position)

        override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */
        }
    }
}

fun String.toDateFormat(): DateFormat = SimpleDateFormat(this, Locale.getDefault())

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()