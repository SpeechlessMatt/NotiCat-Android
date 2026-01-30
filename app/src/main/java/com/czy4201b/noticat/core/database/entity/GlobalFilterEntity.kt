package com.czy4201b.noticat.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_filters")
data class GlobalFilterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val pattern: String,
    val isIgnoreCase: Boolean
)