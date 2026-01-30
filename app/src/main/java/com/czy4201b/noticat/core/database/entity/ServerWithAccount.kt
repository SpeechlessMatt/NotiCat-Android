package com.czy4201b.noticat.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ServerWithAccount(
    @Embedded val server: ServerEntity,
    @Relation(
        parentColumn = "stringId",
        entityColumn = "serverId"
    )
    val account: AccountEntity?
)