package com.flagquiz.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flagquiz.app.data.local.dao.CountryDao
import com.flagquiz.app.data.local.dao.QuizHistoryDao
import com.flagquiz.app.data.local.entity.CountryEntity
import com.flagquiz.app.data.local.entity.QuizHistoryEntity

@Database(
    entities = [CountryEntity::class, QuizHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FlagDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun quizHistoryDao(): QuizHistoryDao

    companion object {
        const val DATABASE_NAME = "flag_quiz_master.db"
    }
}