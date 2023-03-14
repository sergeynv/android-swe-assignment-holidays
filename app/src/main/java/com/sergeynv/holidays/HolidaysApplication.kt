package com.sergeynv.holidays

import android.app.Application
import android.util.Log

class HolidaysApplication: Application() {
    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()
        instance = this
    }

    override fun onTerminate() {
        Log.d(TAG, "onTerminate()")
        super.onTerminate()
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory()")
        super.onLowMemory()
    }

    companion object {
        private const val TAG = "HolidaysApplication"

        lateinit var instance: HolidaysApplication
            private set
    }
}