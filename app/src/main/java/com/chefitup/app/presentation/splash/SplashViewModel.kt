package com.chefitup.app.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.data.local.preferences.OnboardingPreferences
import com.chefitup.app.data.local.preferences.SessionPreferences
import com.chefitup.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashDestination {
    Loading,
    Onboarding,
    Login,
    Home
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    private val sessionPreferences: SessionPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        resolveDestination()
    }

    private fun resolveDestination() {
        viewModelScope.launch {
            val onboardingDone = onboardingPreferences.isOnboardingCompleted.first()
            if (!onboardingDone) {
                _destination.value = SplashDestination.Onboarding
                return@launch
            }

            val rememberSession = sessionPreferences.rememberSession.first()
            val currentUser = authRepository.currentUser

            if (currentUser != null && !rememberSession) {
                authRepository.logout()
                _destination.value = SplashDestination.Login
                return@launch
            }

            _destination.value = if (currentUser != null) {
                SplashDestination.Home
            } else {
                SplashDestination.Login
            }
        }
    }
}
