package com.chefitup.app.domain.model

data class AuthUser(
    val userId: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?
)

data class RegistrationData(
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val username: String,
    val password: String
)

enum class PasswordStrength {
    EMPTY,
    WEAK,
    FAIR,
    GOOD,
    STRONG
}

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val messageResId: Int, val debugMessage: String? = null) : AuthResult()
}
