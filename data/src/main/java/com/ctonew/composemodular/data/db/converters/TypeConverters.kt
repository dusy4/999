package com.ctonew.composemodular.data.db.converters

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class TypeConverters {
    private val moshi = Moshi.Builder().build()
    private val stringListAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    @TypeConverter
    fun listToString(list: List<String>?): String? {
        return if (list == null) null else stringListAdapter.toJson(list)
    }

    @TypeConverter
    fun stringToList(json: String?): List<String>? {
        return if (json == null) null else stringListAdapter.fromJson(json)
    }
}
