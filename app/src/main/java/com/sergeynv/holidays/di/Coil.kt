package com.sergeynv.holidays.di

import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.sergeynv.holidays.HolidaysApplication

private const val DEBUG = true

val imageLoader: ImageLoader by lazy {
    val context = HolidaysApplication.instance
    ImageLoader.Builder(context).apply {
        memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("coil"))
                .maxSizePercent(0.02)
                .build()
        }
        if (DEBUG) {
            // tag:RealImageLoader
            logger(DebugLogger())
        }
    }.build()
}