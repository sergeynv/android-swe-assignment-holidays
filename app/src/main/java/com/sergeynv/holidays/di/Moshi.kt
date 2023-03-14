package com.sergeynv.holidays.di

import com.sergeynv.holidays.ui.toDateFormat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.net.URL
import java.util.Date

private object MoshiUrlAdapter : JsonAdapter<URL>() {
    override fun fromJson(reader: JsonReader): URL = URL(reader.nextString())

    override fun toJson(writer: JsonWriter, value: URL?) {
        writer.value(value?.toString())
    }
}

private object MoshiDateAdapter : JsonAdapter<Date>() {
    val dateFormat = "yyyy-MM-dd".toDateFormat()

    override fun fromJson(reader: JsonReader): Date =
        dateFormat.parse(reader.nextString())!!

    override fun toJson(writer: JsonWriter, value: Date?) {
        value?.let { writer.value(dateFormat.format(it)) }
    }
}

internal val moshi by lazy {
    Moshi.Builder()
        .add(URL::class.java, MoshiUrlAdapter)
        .add(Date::class.java, MoshiDateAdapter)
        .build()
}
