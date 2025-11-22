package com.vincentlarkin.mentzertracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WorkoutReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val prefs = NotificationHelper.loadPreferences(applicationContext)
        if (!prefs.enabled) {
            NotificationHelper.cancelNotifications(applicationContext)
            return Result.success()
        }
        NotificationHelper.showReminderNotification(applicationContext)
        return Result.success()
    }
}

