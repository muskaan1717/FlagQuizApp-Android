package com.flagquiz.app.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.flagquiz.app.core.notification.NotificationHelper
import com.flagquiz.app.data.local.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val preferences = UserPreferencesDataStore(context)
            val todayStr = LocalDate.now().toString()
            val lastDailyDate = preferences.lastDailyDate.first()

            if (lastDailyDate != todayStr) {
                val currentStreak = preferences.dailyStreak.first()
                NotificationHelper.showDailyReminderNotification(context, currentStreak)
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error executing DailyReminderWorker")
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "DailyChallengeReminderWork"

        fun scheduleReminder(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(12, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancelReminder(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}