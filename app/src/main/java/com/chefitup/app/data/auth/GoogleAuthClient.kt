package com.chefitup.app.data.auth

import android.content.Context
import com.chefitup.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isConfigured: Boolean
        get() = context.getString(R.string.google_web_client_id).isNotBlank()

    val signInClient: GoogleSignInClient by lazy {
        val webClientId = context.getString(R.string.google_web_client_id)
        require(webClientId.isNotBlank()) {
            "Google Sign-In is not configured yet. Enable Google in Firebase Auth and set google_web_client_id."
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    suspend fun signOut() {
        if (!isConfigured) return
        try {
            signInClient.signOut().await()
        } catch (_: Exception) {
            // Ignore Google client sign-out failures; Firebase logout still proceeds.
        }
    }
}
