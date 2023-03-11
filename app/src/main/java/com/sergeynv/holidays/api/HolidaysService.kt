package com.sergeynv.holidays.api

import com.sergeynv.holidays.data.ListCountriesResponse
import com.sergeynv.holidays.data.ListHolidaysResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


interface HolidaysService {
    @GET("countries")
    suspend fun getCountries(): ListCountriesResponse

    @GET("countries")
    suspend fun searchCountries(
        @Query("search") search: String
    ): ListCountriesResponse

    @GET("holidays")
    suspend fun getHolidays(
        @Query("country") countryCode: String,
        @Query("year") year: Int = 2022,
    ): ListHolidaysResponse

    companion object {
        private const val HOLIDAYS_API_BASE_URL = "https://holidayapi.com/v1/"
        private const val HOLIDAYS_API_KEY = "8780ae23-9cdd-4616-8b4e-c2b3107c2cdd"
        private const val HOLIDAYS_API_DATE_FORMAT = "yyyy-MM-dd"

        private val moshi by lazy {
            val urlAdapter = object : JsonAdapter<URL>() {
                override fun fromJson(reader: JsonReader): URL = URL(reader.nextString())
                override fun toJson(writer: JsonWriter, value: URL?) {
                    writer.value(value?.toString())
                }
            }

            val dateAdapter = object : JsonAdapter<Date>() {
                val dateFormat = SimpleDateFormat(HOLIDAYS_API_DATE_FORMAT, Locale.US)

                override fun fromJson(reader: JsonReader): Date =
                    dateFormat.parse(reader.nextString())!!

                override fun toJson(writer: JsonWriter, value: Date?) {
                    value?.let { writer.value(dateFormat.format(it)) }
                }
            }

            Moshi.Builder()
                .add(URL::class.java, urlAdapter)
                .add(Date::class.java, dateAdapter)
                .build()
        }

        val retrofit: HolidaysService by lazy {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = BASIC })
                .addInterceptor { chain ->
                    with(chain.request()) {
                        val url = url.newBuilder()
                            .addQueryParameter(name = "key", value = HOLIDAYS_API_KEY)
                            .build()
                        newBuilder()
                            .url(url)
                            .build()
                    }.let { chain.proceed(it) }
                }
                .build()

            Retrofit.Builder()
                .baseUrl(HOLIDAYS_API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(HolidaysService::class.java)
        }
    }
}