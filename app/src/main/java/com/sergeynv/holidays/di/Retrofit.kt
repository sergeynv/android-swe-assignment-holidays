package com.sergeynv.holidays.di

import com.sergeynv.holidays.api.HolidaysService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val HOLIDAYS_API_BASE_URL = "https://holidayapi.com/v1/"
private const val HOLIDAYS_API_KEY = "8780ae23-9cdd-4616-8b4e-c2b3107c2cdd"

private val okHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .addInterceptor { chain ->
            with(chain.request()) {
                val url = url.newBuilder()
                    .addQueryParameter(name = "key", value = HOLIDAYS_API_KEY)
                    .build()
                newBuilder().url(url).build()
            }.let { chain.proceed(it) }
        }
        .build()
}

val retrofit: HolidaysService by lazy {
    Retrofit.Builder()
        .baseUrl(HOLIDAYS_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(HolidaysService::class.java)
}
