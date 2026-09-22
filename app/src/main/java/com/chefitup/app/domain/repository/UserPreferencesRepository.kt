package com.chefitup.app.domain.repository

import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.CookingSkill
import com.chefitup.app.domain.model.ThemeMode
import com.chefitup.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun getSnapshot(): UserPreferences
    suspend fun setThemeMode(mode: ThemeMode): ApiOutcome<Unit>
    suspend fun setLanguage(languageCode: String): ApiOutcome<Unit>
    suspend fun setDiet(diet: String): ApiOutcome<Unit>
    suspend fun setAllergies(allergies: List<String>): ApiOutcome<Unit>
    suspend fun setIntolerances(intolerances: List<String>): ApiOutcome<Unit>
    suspend fun setPreferredFoods(foods: List<String>): ApiOutcome<Unit>
    suspend fun setExcludedIngredients(ingredients: List<String>): ApiOutcome<Unit>
    suspend fun setDislikedFoods(foods: List<String>): ApiOutcome<Unit>
    suspend fun setNotificationsEnabled(enabled: Boolean): ApiOutcome<Unit>
    suspend fun setMealRemindersEnabled(enabled: Boolean): ApiOutcome<Unit>
    suspend fun setRecipeRecommendationsEnabled(enabled: Boolean): ApiOutcome<Unit>
    suspend fun setBadgeNotificationsEnabled(enabled: Boolean): ApiOutcome<Unit>
    suspend fun setCookingSkill(skill: CookingSkill): ApiOutcome<Unit>
    suspend fun updateAll(preferences: UserPreferences): ApiOutcome<Unit>
}
