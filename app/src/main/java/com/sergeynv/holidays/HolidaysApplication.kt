package com.sergeynv.holidays

import android.app.Application
import android.util.Log

class HolidaysApplication : Application() {
    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) {
            System.setProperty(
                /* key */ kotlinx.coroutines.DEBUG_PROPERTY_NAME,
                /* value */ kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
            )
        }
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