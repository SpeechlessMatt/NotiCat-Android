package com.czy4201b.noticat.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = ServerEntity::class,
            parentColumns = ["stringId"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serverId"], unique = true)]
)
data class AccountEntity(
    @PrimaryKey
    val serverId: String,
    val loginAuth: String,
)