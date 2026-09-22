package com.chefitup.app.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.domain.model.AuthResult
import com.chefitup.app.domain.model.PasswordStrength
import com.chefitup.app.domain.model.RegistrationData
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

data class RegisterUiState(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.EMPTY,
    val isLoading: Boolean = false,
    val nameError: Int? = null,
    val surnameError: Int? = null,
    val emailError: Int? = null,
    val phoneError: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val formError: Int? = null
)

sealed class RegisterEvent {
    data object NavigateToHome : RegisterEvent()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RegisterEvent>()
    val events: SharedFlow<RegisterEvent> = _events.asSharedFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, formError = null) }
    }

    fun onSurnameChange(value: String) {
        _uiState.update { it.copy(surname = value, surnameError = null, formError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, phoneError = null, formError = null) }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null, formError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                formError = null,
                passwordStrength = AuthValidator.passwordStrength(value)
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, formError = null)
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    }

    fun register() {
        val state = _uiState.value
        val nameError = AuthValidator.validateName(state.name)
        val surnameError = AuthValidator.validateSurname(state.surname)
        val emailError = AuthValidator.validateEmail(state.email)
        val phoneError = AuthValidator.validatePhone(state.phoneNumber)
        val usernameError = AuthValidator.validateUsername(state.username)
        val passwordError = AuthValidator.validatePassword(state.password)
        val confirmError = AuthValidator.validateConfirmPassword(
            state.password,
            state.confirmPassword
        )

        if (listOf(
                nameError, surnameError, emailError, phoneError,
                usernameError, passwordError, confirmError
            ).any { it != null }
        ) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    surnameError = surnameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    usernameError = usernameError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formError = null) }

            val result = authRepository.register(
                RegistrationData(
                    name = state.name,
                    surname = state.surname,
                    email = state.email,
                    phoneNumber = state.phoneNumber,
                    username = state.username,
                    password = state.password
                )
            )

            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(RegisterEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, formError = result.messageResId)
                    }
                }
            }
        }
    }
}
