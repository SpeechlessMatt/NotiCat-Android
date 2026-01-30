package com.czy4201b.noticat.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.czy4201b.noticat.core.database.entity.AccountEntity
import com.czy4201b.noticat.core.database.entity.GlobalFilterEntity
import com.czy4201b.noticat.core.database.entity.ServerEntity

@Database(
    entities = [
        ServerEntity::class,
        AccountEntity::class,
        GlobalFilterEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NotiCatDatabase : RoomDatabase() {
    abstract val serverDao: ServerDao
    abstract val accountDao: AccountDao
    abstract val globalFilterDao: GlobalFilterDao
}