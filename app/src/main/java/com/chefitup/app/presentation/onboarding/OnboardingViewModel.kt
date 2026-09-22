package com.chefitup.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.data.local.preferences.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val pageCount: Int = onboardingPages.size
) {
    val isFirstPage: Boolean get() = currentPage == 0
    val isLastPage: Boolean get() = currentPage == pageCount - 1
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextPage() {
        _uiState.update { state ->
            if (state.isLastPage) state
            else state.copy(currentPage = state.currentPage + 1)
        }
    }

    fun previousPage() {
        _uiState.update { state ->
            if (state.isFirstPage) state
            else state.copy(currentPage = state.currentPage - 1)
        }
    }

    fun goToPage(index: Int) {
        if (index in onboardingPages.indices) {
            _uiState.update { it.copy(currentPage = index) }
        }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted(true)
            onFinished()
        }
    }
}
