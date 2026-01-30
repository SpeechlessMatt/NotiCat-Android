package com.czy4201b.noticat.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.czy4201b.noticat.core.database.entity.GlobalFilterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalFilterDao {
    @Query("SELECT * FROM global_filters")
    fun getAll(): Flow<List<GlobalFilterEntity>>

    @Query("SELECT * FROM global_filters")
    suspend fun getAllStatic(): List<GlobalFilterEntity>

    @Upsert
    suspend fun insert(filter: GlobalFilterEntity)

    @Upsert
    suspend fun insertAll(filters: List<GlobalFilterEntity>)

    @Query("DELETE FROM global_filters")
    suspend fun deleteAllFields()
}