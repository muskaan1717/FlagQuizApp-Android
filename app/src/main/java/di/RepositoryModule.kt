package com.flagquiz.app.di

import com.flagquiz.app.data.repository.CountryRepositoryImpl
import com.flagquiz.app.data.repository.QuizHistoryRepositoryImpl
import com.flagquiz.app.domain.repository.CountryRepository
import com.flagquiz.app.domain.repository.QuizHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCountryRepository(impl: CountryRepositoryImpl): CountryRepository

    @Binds
    @Singleton
    abstract fun bindQuizHistoryRepository(impl: QuizHistoryRepositoryImpl): QuizHistoryRepository
}