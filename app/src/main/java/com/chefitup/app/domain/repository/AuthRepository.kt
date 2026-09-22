package com.chefitup.app.domain.repository

import com.chefitup.app.domain.model.AuthResult
import com.chefitup.app.domain.model.AuthUser
import com.chefitup.app.domain.model.RegistrationData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: AuthUser?
    val authState: Flow<AuthUser?>

    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(data: RegistrationData): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun sendPasswordReset(email: String): AuthResult
    suspend fun logout()
}
