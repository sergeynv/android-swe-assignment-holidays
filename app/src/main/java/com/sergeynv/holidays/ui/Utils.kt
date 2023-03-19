package com.sergeynv.holidays.ui

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.sergeynv.holidays.data.Country
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

private const val LOG_IMAGE_REQUEST_EVENTS = true

fun ImageView.loadCountryFlag(country: Country) = load(country.flagUrl.toString()) {
    if (LOG_IMAGE_REQUEST_EVENTS) listener(ImageRequestLoggingListener)
}

private object ImageRequestLoggingListener : ImageRequest.Listener {
    private const val TAG = "ImageRequest.Listener"

    override fun onCancel(request: ImageRequest) {
        Log.d(TAG, "CANCELLED request for '${request.data}'")
    }

    override fun onError(request: ImageRequest, result: ErrorResult) {
        Log.d(TAG, "ERRORED request for '${request.data}': ${result.throwable}")
    }

    override fun onStart(request: ImageRequest) {
        Log.d(TAG, "STARTED request for '${request.data}'")
    }

    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        Log.d(TAG, "LOADED '${request.data}': $result")
    }
}