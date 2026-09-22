package com.chefitup.app.data.auth

import com.chefitup.app.R
import com.chefitup.app.data.firestore.FirestoreUserSync
import com.chefitup.app.data.local.preferences.SessionPreferences
import com.chefitup.app.domain.model.AuthResult
import com.chefitup.app.domain.model.AuthUser
import com.chefitup.app.domain.model.RegistrationData
import com.chefitup.app.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val sessionPreferences: SessionPreferences,
    private val firestoreUserSync: FirestoreUserSync
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.toAuthUser()

    override val authState: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()
            val user = result.user?.toAuthUser()
                ?: return AuthResult.Error(R.string.auth_error_generic)
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapException(e), e.message)
        }
    }

    override suspend fun register(data: RegistrationData): AuthResult {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(data.email.trim(), data.password)
                .await()

            val firebaseUser = result.user
                ?: return AuthResult.Error(R.string.auth_error_generic)

            val displayName = "${data.name.trim()} ${data.surname.trim()}".trim()
            firebaseUser.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
            ).await()

            // Password is never stored — Firebase Auth hashes it server-side.
            sessionPreferences.saveUserProfile(
                name = data.name.trim(),
                surname = data.surname.trim(),
                username = data.username.trim(),
                phoneNumber = data.phoneNumber.trim().replace(" ", ""),
                email = data.email.trim()
            )
            runCatching {
                firestoreUserSync.pushProfile(
                    name = data.name.trim(),
                    surname = data.surname.trim(),
                    username = data.username.trim(),
                    phone = data.phoneNumber.trim().replace(" ", ""),
                    diet = "",
                    allergies = emptyList(),
                    languageCode = "en",
                    theme = "SYSTEM"
                )
            }

            AuthResult.Success(firebaseUser.toAuthUser().copy(displayName = displayName))
        } catch (e: Exception) {
            AuthResult.Error(mapException(e), e.message)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.toAuthUser()
                ?: return AuthResult.Error(R.string.auth_error_generic)

            val parts = user.displayName?.split(" ").orEmpty()
            sessionPreferences.saveUserProfile(
                name = parts.getOrNull(0).orEmpty(),
                surname = parts.drop(1).joinToString(" "),
                username = user.email?.substringBefore("@").orEmpty(),
                phoneNumber = "",
                email = user.email.orEmpty()
            )
            runCatching {
                firestoreUserSync.pushProfile(
                    name = parts.getOrNull(0).orEmpty(),
                    surname = parts.drop(1).joinToString(" "),
                    username = user.email?.substringBefore("@").orEmpty(),
                    phone = "",
                    diet = "",
                    allergies = emptyList(),
                    languageCode = "en",
                    theme = "SYSTEM"
                )
            }

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapException(e), e.message)
        }
    }

    override suspend fun sendPasswordReset(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(
                AuthUser(userId = "", email = email.trim(), displayName = null, photoUrl = null)
            )
        } catch (e: Exception) {
            AuthResult.Error(mapException(e), e.message)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        sessionPreferences.clearUserProfile()
    }

    private fun mapException(e: Exception): Int = when (e) {
        is FirebaseAuthInvalidCredentialsException -> R.string.auth_error_wrong_credentials
        is FirebaseAuthInvalidUserException -> R.string.auth_error_user_not_found
        is FirebaseAuthUserCollisionException -> R.string.auth_error_email_in_use
        is FirebaseAuthWeakPasswordException -> R.string.auth_error_weak_password
        is FirebaseNetworkException -> R.string.auth_error_network
        is IllegalStateException -> R.string.auth_error_firebase_not_configured
        else -> R.string.auth_error_generic
    }

    private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
        userId = uid,
        email = email.orEmpty(),
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )
}
