package com.czy4201b.noticat.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.czy4201b.noticat.core.database.entity.ServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    // 按时间顺序去获取数据
    @Query("SELECT * FROM servers ORDER BY createdAt ASC")
    fun getAll(): Flow<List<ServerEntity>>

    @Upsert
    suspend fun insert(server: ServerEntity)

    @Upsert
    suspend fun insertAll(servers: List<ServerEntity>)

    @Query("SELECT * FROM servers WHERE stringId = :stringId LIMIT 1")
    suspend fun getServerByStringId(stringId: String): ServerEntity?

    @Query("DELETE FROM servers WHERE stringId = :stringId")
    suspend fun deleteByStringId(stringId: String)
}