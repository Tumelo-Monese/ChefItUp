package com.chefitup.app.domain.validation

import com.chefitup.app.R
import com.chefitup.app.domain.model.PasswordStrength

object AuthValidator {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val usernameRegex = Regex("^[A-Za-z0-9_]{3,20}$")
    // South African mobile: 0XXXXXXXXX or +27XXXXXXXXX
    private val phoneRegex = Regex("^(\\+27|0)[6-8][0-9]{8}$")

    fun validateEmail(email: String): Int? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> R.string.auth_error_empty_email
            !emailRegex.matches(trimmed) -> R.string.auth_error_invalid_email
            else -> null
        }
    }

    fun validatePassword(password: String, requireMinLength: Boolean = true): Int? {
        return when {
            password.isEmpty() -> R.string.auth_error_empty_password
            requireMinLength && password.length < 8 -> R.string.auth_error_password_short
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Int? {
        return when {
            confirmPassword.isEmpty() -> R.string.auth_error_empty_password
            password != confirmPassword -> R.string.auth_error_password_mismatch
            else -> null
        }
    }

    fun validateName(name: String): Int? =
        if (name.trim().isEmpty()) R.string.auth_error_empty_name else null

    fun validateSurname(surname: String): Int? =
        if (surname.trim().isEmpty()) R.string.auth_error_empty_surname else null

    fun validateUsername(username: String): Int? {
        val trimmed = username.trim()
        return when {
            trimmed.isEmpty() -> R.string.auth_error_empty_username
            !usernameRegex.matches(trimmed) -> R.string.auth_error_username_invalid
            else -> null
        }
    }

    fun validatePhone(phone: String): Int? {
        val normalised = phone.trim().replace(" ", "")
        return when {
            normalised.isEmpty() -> R.string.auth_error_empty_phone
            !phoneRegex.matches(normalised) -> R.string.auth_error_phone_invalid
            else -> null
        }
    }

    fun passwordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.EMPTY

        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.any { it.isLowerCase() } && password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score <= 1 -> PasswordStrength.WEAK
            score == 2 -> PasswordStrength.FAIR
            score == 3 || score == 4 -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }
    }
}
