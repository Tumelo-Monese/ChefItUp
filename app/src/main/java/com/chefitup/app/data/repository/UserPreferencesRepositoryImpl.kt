package com.chefitup.app.data.repository

import com.chefitup.app.data.firestore.FirestoreUserSync
import com.chefitup.app.data.local.JsonListConverter
import com.chefitup.app.data.local.dao.UserPreferenceDao
import com.chefitup.app.data.local.entity.UserPreferenceEntity
import com.chefitup.app.data.local.preferences.SessionPreferences
import com.chefitup.app.data.local.preferences.UserPreferencesDataStore
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.CookingSkill
import com.chefitup.app.domain.model.ThemeMode
import com.chefitup.app.domain.model.UserPreferences
import com.chefitup.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val userPreferenceDao: UserPreferenceDao,
    private val sessionPreferences: SessionPreferences,
    private val firestoreUserSync: FirestoreUserSync
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.preferences

    override suspend fun getSnapshot(): UserPreferences = preferences.first()

    override suspend fun setThemeMode(mode: ThemeMode): ApiOutcome<Unit> =
        update { it.copy(themeMode = mode) }

    override suspend fun setLanguage(languageCode: String): ApiOutcome<Unit> =
        update { it.copy(languageCode = languageCode) }

    override suspend fun setDiet(diet: String): ApiOutcome<Unit> =
        update { it.copy(diet = diet) }

    override suspend fun setAllergies(allergies: List<String>): ApiOutcome<Unit> =
        update { it.copy(allergies = allergies) }

    override suspend fun setIntolerances(intolerances: List<String>): ApiOutcome<Unit> =
        update { it.copy(intolerances = intolerances) }

    override suspend fun setPreferredFoods(foods: List<String>): ApiOutcome<Unit> =
        update { it.copy(preferredFoods = foods) }

    override suspend fun setExcludedIngredients(ingredients: List<String>): ApiOutcome<Unit> =
        update { it.copy(excludedIngredients = ingredients) }

    override suspend fun setDislikedFoods(foods: List<String>): ApiOutcome<Unit> =
        update { it.copy(dislikedFoods = foods) }

    override suspend fun setNotificationsEnabled(enabled: Boolean): ApiOutcome<Unit> =
        update { it.copy(notificationsEnabled = enabled) }

    override suspend fun setMealRemindersEnabled(enabled: Boolean): ApiOutcome<Unit> =
        update { it.copy(mealRemindersEnabled = enabled) }

    override suspend fun setRecipeRecommendationsEnabled(enabled: Boolean): ApiOutcome<Unit> =
        update { it.copy(recipeRecommendationsEnabled = enabled) }

    override suspend fun setBadgeNotificationsEnabled(enabled: Boolean): ApiOutcome<Unit> =
        update { it.copy(badgeNotificationsEnabled = enabled) }

    override suspend fun setCookingSkill(skill: CookingSkill): ApiOutcome<Unit> =
        update { it.copy(cookingSkill = skill) }

    override suspend fun updateAll(preferences: UserPreferences): ApiOutcome<Unit> =
        update { preferences }

    private suspend fun update(transform: (UserPreferences) -> UserPreferences): ApiOutcome<Unit> {
        dataStore.update(transform)
        val snapshot = getSnapshot()
        mirrorToRoom(snapshot)
        pushToFirestore(snapshot)
        return ApiOutcome.Success(Unit)
    }

    private suspend fun pushToFirestore(prefs: UserPreferences) {
        val profile = sessionPreferences.userProfile.first()
        runCatching {
            firestoreUserSync.pushProfile(
                name = profile.name,
                surname = profile.surname,
                username = profile.username,
                phone = profile.phoneNumber,
                diet = prefs.diet,
                allergies = prefs.allergies + prefs.intolerances,
                languageCode = prefs.languageCode,
                theme = prefs.themeMode.name
            )
        }
    }

    private suspend fun mirrorToRoom(prefs: UserPreferences) {
        userPreferenceDao.upsert(
            UserPreferenceEntity(
                id = 1,
                themeMode = prefs.themeMode.name,
                languageCode = prefs.languageCode,
                diet = prefs.diet,
                allergiesJson = JsonListConverter.stringListToJson(prefs.allergies),
                intolerancesJson = JsonListConverter.stringListToJson(prefs.intolerances),
                preferredFoodsJson = JsonListConverter.stringListToJson(prefs.preferredFoods),
                excludedIngredientsJson = JsonListConverter.stringListToJson(prefs.excludedIngredients),
                dislikedFoodsJson = JsonListConverter.stringListToJson(prefs.dislikedFoods),
                notificationsEnabled = prefs.notificationsEnabled,
                mealRemindersEnabled = prefs.mealRemindersEnabled,
                recipeRecommendationsEnabled = prefs.recipeRecommendationsEnabled,
                badgeNotificationsEnabled = prefs.badgeNotificationsEnabled,
                cookingSkill = prefs.cookingSkill.name
            )
        )
    }
}
