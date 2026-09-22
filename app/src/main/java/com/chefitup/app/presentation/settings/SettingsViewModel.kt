package com.chefitup.app.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.data.auth.GoogleAuthClient
import com.chefitup.app.domain.model.ThemeMode
import com.chefitup.app.domain.model.UserPreferences
import com.chefitup.app.domain.repository.AuthRepository
import com.chefitup.app.domain.repository.UserPreferencesRepository
import com.chefitup.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isSaving: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
                ReminderScheduler.sync(
                    context = appContext,
                    mealReminders = prefs.notificationsEnabled && prefs.mealRemindersEnabled,
                    recipeRecommendations = prefs.notificationsEnabled && prefs.recipeRecommendationsEnabled
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch { preferencesRepository.setLanguage(code) }
    }

    fun setDiet(diet: String) {
        viewModelScope.launch { preferencesRepository.setDiet(diet) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setNotificationsEnabled(enabled) }
    }

    fun setMealRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setMealRemindersEnabled(enabled) }
    }

    fun setRecipeRecommendationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setRecipeRecommendationsEnabled(enabled) }
    }

    fun setBadgeNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setBadgeNotificationsEnabled(enabled) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            googleAuthClient.signOut()
            authRepository.logout()
            onLoggedOut()
        }
    }
}
