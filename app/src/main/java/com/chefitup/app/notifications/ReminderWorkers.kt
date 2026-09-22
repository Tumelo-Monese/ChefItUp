package com.chefitup.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.chefitup.app.R
import com.chefitup.app.domain.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val MEAL_WORK = "meal_reminders"
    const val RECIPE_WORK = "recipe_recommendations"
    const val CHANNEL_ID = "chefitup_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    fun sync(context: Context, mealReminders: Boolean, recipeRecommendations: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (mealReminders) {
            val request = PeriodicWorkRequestBuilder<MealReminderWorker>(12, TimeUnit.HOURS)
                .build()
            wm.enqueueUniquePeriodicWork(MEAL_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            wm.cancelUniqueWork(MEAL_WORK)
        }
        if (recipeRecommendations) {
            val request = PeriodicWorkRequestBuilder<RecipeRecommendationWorker>(24, TimeUnit.HOURS)
                .build()
            wm.enqueueUniquePeriodicWork(RECIPE_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            wm.cancelUniqueWork(RECIPE_WORK)
        }
    }
}

@HiltWorker
class MealReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = preferencesRepository.preferences.first()
        if (!prefs.notificationsEnabled || !prefs.mealRemindersEnabled) {
            return Result.success()
        }
        ReminderScheduler.ensureChannel(applicationContext)
        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chef_logo)
            .setContentTitle(applicationContext.getString(R.string.meal_reminder_title))
            .setContentText(applicationContext.getString(R.string.meal_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(5101, notification)
        return Result.success()
    }
}

@HiltWorker
class RecipeRecommendationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = preferencesRepository.preferences.first()
        if (!prefs.notificationsEnabled || !prefs.recipeRecommendationsEnabled) {
            return Result.success()
        }
        ReminderScheduler.ensureChannel(applicationContext)
        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chef_logo)
            .setContentTitle(applicationContext.getString(R.string.recipe_recommendation_title))
            .setContentText(applicationContext.getString(R.string.recipe_recommendation_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(5102, notification)
        return Result.success()
    }
}
