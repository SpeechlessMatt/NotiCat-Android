package com.czy4201b.noticat.core.common

import com.czy4201b.noticat.core.database.GlobalFilterDao
import com.czy4201b.noticat.core.database.entity.GlobalFilterEntity

object GlobalFilterManager {
    private lateinit var globalFilterDao: GlobalFilterDao

    fun init(dao: GlobalFilterDao){
        this.globalFilterDao = dao
    }

    suspend fun getGlobalFilters(): List<GlobalFilterEntity> {
        return globalFilterDao.getAllStatic()
    }

}