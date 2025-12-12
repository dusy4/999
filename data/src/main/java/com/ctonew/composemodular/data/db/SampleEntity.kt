package com.ctonew.composemodular.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sample")
data class SampleEntity(
    @PrimaryKey val id: String,
    val value: String,
)
