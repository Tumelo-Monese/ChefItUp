package com.chefitup.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        gson.toJson(value.orEmpty())

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}

object JsonListConverter {
    private val gson = Gson()

    fun stringListToJson(list: List<String>): String = gson.toJson(list)

    fun jsonToStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun <T> toJson(value: T): String = gson.toJson(value)

    fun <T> fromJson(json: String?, typeToken: TypeToken<T>): T? {
        if (json.isNullOrBlank()) return null
        return gson.fromJson(json, typeToken.type)
    }
}
