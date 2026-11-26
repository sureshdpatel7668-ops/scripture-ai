package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.repository.ScriptureRepository

class ScriptureApplication : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
        lateinit var repository: ScriptureRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "scripture_ai_db"
        ).fallbackToDestructiveMigration().build()
        repository = ScriptureRepository(database.scriptureDao())
    }
}
