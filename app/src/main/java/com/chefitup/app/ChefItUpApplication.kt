package com.chefitup.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.chefitup.app.domain.repository.SyncRepository
import com.chefitup.app.domain.repository.UserPreferencesRepository
import com.chefitup.app.notifications.ReminderScheduler
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ChefItUpApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncRepository: SyncRepository

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        syncRepository.schedulePeriodicSync()
        ReminderScheduler.ensureChannel(this)
        appScope.launch {
            val prefs = preferencesRepository.preferences.first()
            ReminderScheduler.sync(
                context = this@ChefItUpApplication,
                mealReminders = prefs.notificationsEnabled && prefs.mealRemindersEnabled,
                recipeRecommendations = prefs.notificationsEnabled && prefs.recipeRecommendationsEnabled
            )
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
