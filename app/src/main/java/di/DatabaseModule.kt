package com.flagquiz.app.di

import android.content.Context
import androidx.room.Room
import com.flagquiz.app.data.local.FlagDatabase
import com.flagquiz.app.data.local.dao.CountryDao
import com.flagquiz.app.data.local.dao.QuizHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFlagDatabase(
        @ApplicationContext context: Context
    ): FlagDatabase {
        return Room.databaseBuilder(
            context,
            FlagDatabase::class.java,
            FlagDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCountryDao(database: FlagDatabase): CountryDao = database.countryDao()

    @Provides
    fun provideQuizHistoryDao(database: FlagDatabase): QuizHistoryDao = database.quizHistoryDao()
}