package com.chefitup.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "chefitup_session"
)

data class LocalUserProfile(
    val name: String = "",
    val surname: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)

@Singleton
class SessionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val rememberSessionKey = booleanPreferencesKey("remember_session")
    private val nameKey = stringPreferencesKey("profile_name")
    private val surnameKey = stringPreferencesKey("profile_surname")
    private val usernameKey = stringPreferencesKey("profile_username")
    private val phoneKey = stringPreferencesKey("profile_phone")
    private val emailKey = stringPreferencesKey("profile_email")

    val rememberSession: Flow<Boolean> = context.sessionDataStore.data.map { prefs ->
        prefs[rememberSessionKey] ?: true
    }

    val userProfile: Flow<LocalUserProfile> = context.sessionDataStore.data.map { prefs ->
        LocalUserProfile(
            name = prefs[nameKey].orEmpty(),
            surname = prefs[surnameKey].orEmpty(),
            username = prefs[usernameKey].orEmpty(),
            phoneNumber = prefs[phoneKey].orEmpty(),
            email = prefs[emailKey].orEmpty()
        )
    }

    suspend fun setRememberSession(remember: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[rememberSessionKey] = remember
        }
    }

    suspend fun saveUserProfile(
        name: String,
        surname: String,
        username: String,
        phoneNumber: String,
        email: String
    ) {
        context.sessionDataStore.edit { prefs ->
            prefs[nameKey] = name
            prefs[surnameKey] = surname
            prefs[usernameKey] = username
            prefs[phoneKey] = phoneNumber
            prefs[emailKey] = email
        }
    }

    suspend fun clearUserProfile() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(nameKey)
            prefs.remove(surnameKey)
            prefs.remove(usernameKey)
            prefs.remove(phoneKey)
            prefs.remove(emailKey)
        }
    }
}
