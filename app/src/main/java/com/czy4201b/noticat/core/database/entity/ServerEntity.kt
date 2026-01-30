package com.czy4201b.noticat.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "servers",
    indices = [Index(value = ["stringId"], unique = true)]
)
data class ServerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stringId: String,
    val url: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)