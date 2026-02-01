package com.czy4201b.noticat.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.czy4201b.noticat.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    // 按时间顺序去获取数据
    @Query("SELECT * FROM accounts")
    fun getAll(): Flow<List<AccountEntity>>

    @Upsert
    suspend fun insert(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: String)

    @Query("SELECT * FROM accounts WHERE serverId = :serverId LIMIT 1")
    suspend fun getAccountByServerId(serverId: String): AccountEntity?
}