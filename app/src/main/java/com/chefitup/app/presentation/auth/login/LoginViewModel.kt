package com.chefitup.app.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.R
import com.chefitup.app.data.auth.GoogleAuthClient
import com.chefitup.app.data.local.preferences.SessionPreferences
import com.chefitup.app.domain.model.AuthResult
import com.chefitup.app.domain.repository.AuthRepository
import com.chefitup.app.domain.validation.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberSession: Boolean = true,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val formError: Int? = null,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordMessage: Int? = null,
    val isSendingReset: Boolean = false
)

sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
    data object LaunchGoogleSignIn : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionPreferences: SessionPreferences,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionPreferences.rememberSession.collect { remember ->
                _uiState.update { it.copy(rememberSession = remember) }
            }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, emailError = null, formError = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, passwordError = null, formError = null)
        }
    }

    fun onRememberSessionChange(value: Boolean) {
        _uiState.update { it.copy(rememberSession = value) }
        viewModelScope.launch {
            sessionPreferences.setRememberSession(value)
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun login() {
        val state = _uiState.value
        val emailError = AuthValidator.validateEmail(state.email)
        val passwordError = AuthValidator.validatePassword(state.password, requireMinLength = false)

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formError = null) }
            sessionPreferences.setRememberSession(state.rememberSession)

            when (val result = authRepository.login(state.email, state.password)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(LoginEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, formError = result.messageResId)
                    }
                }
            }
        }
    }

    fun onGoogleSignInClick() {
        if (!googleAuthClient.isConfigured) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    formError = R.string.auth_error_google_not_configured
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formError = null) }
            _events.emit(LoginEvent.LaunchGoogleSignIn)
        }
    }

    fun onGoogleIdTokenReceived(idToken: String?) {
        if (idToken.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    formError = R.string.auth_error_google_cancelled
                )
            }
            return
        }

        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    sessionPreferences.setRememberSession(_uiState.value.rememberSession)
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(LoginEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, formError = result.messageResId)
                    }
                }
            }
        }
    }

    fun onGoogleSignInFailed() {
        _uiState.update {
            it.copy(isLoading = false, formError = R.string.auth_error_google_failed)
        }
    }

    fun showForgotPasswordDialog() {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = true,
                forgotPasswordEmail = it.email,
                forgotPasswordMessage = null
            )
        }
    }

    fun dismissForgotPasswordDialog() {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = false,
                isSendingReset = false,
                forgotPasswordMessage = null
            )
        }
    }

    fun onForgotPasswordEmailChange(value: String) {
        _uiState.update {
            it.copy(forgotPasswordEmail = value, forgotPasswordMessage = null)
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.forgotPasswordEmail
        val emailError = AuthValidator.validateEmail(email)
        if (emailError != null) {
            _uiState.update { it.copy(forgotPasswordMessage = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingReset = true, forgotPasswordMessage = null) }
            when (val result = authRepository.sendPasswordReset(email)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSendingReset = false,
                            forgotPasswordMessage = R.string.forgot_password_sent
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSendingReset = false,
                            forgotPasswordMessage = result.messageResId
                        )
                    }
                }
            }
        }
    }

    fun googleSignInIntent() = googleAuthClient.signInClient.signInIntent
}
