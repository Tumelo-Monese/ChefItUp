package com.chefitup.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.data.auth.GoogleAuthClient
import com.chefitup.app.domain.model.BadgeDefinition
import com.chefitup.app.domain.model.UserGamification
import com.chefitup.app.domain.repository.AuthRepository
import com.chefitup.app.domain.repository.GamificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val gamification: UserGamification = UserGamification(),
    val badges: List<BadgeDefinition> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gamificationRepository: GamificationRepository,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            displayName = authRepository.currentUser?.displayName
                ?: authRepository.currentUser?.email?.substringBefore('@').orEmpty(),
            email = authRepository.currentUser?.email.orEmpty(),
            photoUrl = authRepository.currentUser?.photoUrl,
            badges = gamificationRepository.allBadges()
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            gamificationRepository.observeGamification().collect { g ->
                _uiState.update { it.copy(gamification = g) }
            }
        }
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            displayName = it.displayName
                                ?: it.email.substringBefore('@'),
                            email = it.email,
                            photoUrl = it.photoUrl
                        )
                    }
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        // Navigate immediately — don't let sign-out logic block or hang the UI
        onLoggedOut()

        // Best-effort cleanup, run separately so it can't stop navigation
        viewModelScope.launch {
            try {
                googleAuthClient.signOut()
            } catch (e: Exception) {
                // ignore — non-blocking
            }
            try {
                authRepository.logout()
            } catch (e: Exception) {
                // ignore — non-blocking
            }
        }
    }}
