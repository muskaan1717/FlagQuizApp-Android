package com.flagquiz.app.data.repository

import com.flagquiz.app.core.common.DispatcherProvider
import com.flagquiz.app.data.local.dao.QuizHistoryDao
import com.flagquiz.app.data.local.entity.QuizHistoryEntity
import com.flagquiz.app.domain.model.QuizHistory
import com.flagquiz.app.domain.repository.QuizHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizHistoryRepositoryImpl @Inject constructor(
    private val dao: QuizHistoryDao,
    private val dispatchers: DispatcherProvider
) : QuizHistoryRepository {

    override fun getHistoryStream(): Flow<List<QuizHistory>> {
        return dao.getAllHistoryStream()
            .map { entities -> entities.map(QuizHistoryEntity::toDomain) }
            .flowOn(dispatchers.io)
    }

    override fun getTopScoresStream(limit: Int): Flow<List<QuizHistory>> {
        return dao.getTopScoresStream(limit)
            .map { entities -> entities.map(QuizHistoryEntity::toDomain) }
            .flowOn(dispatchers.io)
    }

    override suspend fun getHistoryById(id: Long): QuizHistory? = withContext(dispatchers.io) {
        dao.getHistoryById(id)?.toDomain()
    }

    override suspend fun saveQuizResult(history: QuizHistory): Long = withContext(dispatchers.io) {
        dao.insertHistory(QuizHistoryEntity(
            id = history.id,
            timestamp = history.timestamp,
            gameMode = history.gameMode,
            difficulty = history.difficulty,
            score = history.score,
            totalQuestions = history.totalQuestions,
            highestStreak = history.highestStreak,
            timeSpentSeconds = history.timeSpentSeconds,
            missedCountryCodes = history.missedCountryCodes.joinToString(",")
        ))
    }

    override suspend fun clearAllHistory() = withContext(dispatchers.io) {
        dao.clearHistory()
    }
}