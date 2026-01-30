package com.czy4201b.noticat

import android.app.Application
import androidx.room.Room
import com.czy4201b.noticat.core.common.GlobalFilterManager
import com.czy4201b.noticat.core.common.ServerManager
import com.czy4201b.noticat.core.database.NotiCatDatabase

class NotiCatApplication : Application() {

    val database by lazy {
        Room.databaseBuilder(
            this,
            NotiCatDatabase::class.java,
            "noticat.db"
        ).build()
    }

    companion object {
        lateinit var instance: NotiCatApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        ServerManager.init(database.accountDao)
        GlobalFilterManager.init(database.globalFilterDao)
    }
}
