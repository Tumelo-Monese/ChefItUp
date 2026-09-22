package com.chefitup.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chefitup.app.data.local.JsonListConverter
import com.chefitup.app.domain.model.CookingSkill
import com.chefitup.app.domain.model.ThemeMode
import com.chefitup.app.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "chefitup_user_preferences"
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("language_code")
    private val dietKey = stringPreferencesKey("diet")
    private val allergiesKey = stringPreferencesKey("allergies_json")
    private val intolerancesKey = stringPreferencesKey("intolerances_json")
    private val preferredFoodsKey = stringPreferencesKey("preferred_foods_json")
    private val excludedKey = stringPreferencesKey("excluded_ingredients_json")
    private val dislikedKey = stringPreferencesKey("disliked_foods_json")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")
    private val mealRemindersKey = booleanPreferencesKey("meal_reminders_enabled")
    private val recipeRecsKey = booleanPreferencesKey("recipe_recommendations_enabled")
    private val badgeNotifsKey = booleanPreferencesKey("badge_notifications_enabled")
    private val skillKey = stringPreferencesKey("cooking_skill")

    val preferences: Flow<UserPreferences> = context.userPrefsDataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[themeKey]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM)
            } ?: ThemeMode.SYSTEM,
            languageCode = prefs[languageKey] ?: "en",
            diet = prefs[dietKey].orEmpty(),
            allergies = JsonListConverter.jsonToStringList(prefs[allergiesKey]),
            intolerances = JsonListConverter.jsonToStringList(prefs[intolerancesKey]),
            preferredFoods = JsonListConverter.jsonToStringList(prefs[preferredFoodsKey]),
            excludedIngredients = JsonListConverter.jsonToStringList(prefs[excludedKey]),
            dislikedFoods = JsonListConverter.jsonToStringList(prefs[dislikedKey]),
            notificationsEnabled = prefs[notificationsKey] ?: true,
            mealRemindersEnabled = prefs[mealRemindersKey] ?: true,
            recipeRecommendationsEnabled = prefs[recipeRecsKey] ?: true,
            badgeNotificationsEnabled = prefs[badgeNotifsKey] ?: true,
            cookingSkill = prefs[skillKey]?.let {
                runCatching { CookingSkill.valueOf(it) }.getOrDefault(CookingSkill.BEGINNER)
            } ?: CookingSkill.BEGINNER
        )
    }

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        context.userPrefsDataStore.edit { prefs ->
            val current = UserPreferences(
                themeMode = prefs[themeKey]?.let {
                    runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM)
                } ?: ThemeMode.SYSTEM,
                languageCode = prefs[languageKey] ?: "en",
                diet = prefs[dietKey].orEmpty(),
                allergies = JsonListConverter.jsonToStringList(prefs[allergiesKey]),
                intolerances = JsonListConverter.jsonToStringList(prefs[intolerancesKey]),
                preferredFoods = JsonListConverter.jsonToStringList(prefs[preferredFoodsKey]),
                excludedIngredients = JsonListConverter.jsonToStringList(prefs[excludedKey]),
                dislikedFoods = JsonListConverter.jsonToStringList(prefs[dislikedKey]),
                notificationsEnabled = prefs[notificationsKey] ?: true,
                mealRemindersEnabled = prefs[mealRemindersKey] ?: true,
                recipeRecommendationsEnabled = prefs[recipeRecsKey] ?: true,
                badgeNotificationsEnabled = prefs[badgeNotifsKey] ?: true,
                cookingSkill = prefs[skillKey]?.let {
                    runCatching { CookingSkill.valueOf(it) }.getOrDefault(CookingSkill.BEGINNER)
                } ?: CookingSkill.BEGINNER
            )
            val next = transform(current)
            prefs[themeKey] = next.themeMode.name
            prefs[languageKey] = next.languageCode
            prefs[dietKey] = next.diet
            prefs[allergiesKey] = JsonListConverter.stringListToJson(next.allergies)
            prefs[intolerancesKey] = JsonListConverter.stringListToJson(next.intolerances)
            prefs[preferredFoodsKey] = JsonListConverter.stringListToJson(next.preferredFoods)
            prefs[excludedKey] = JsonListConverter.stringListToJson(next.excludedIngredients)
            prefs[dislikedKey] = JsonListConverter.stringListToJson(next.dislikedFoods)
            prefs[notificationsKey] = next.notificationsEnabled
            prefs[mealRemindersKey] = next.mealRemindersEnabled
            prefs[recipeRecsKey] = next.recipeRecommendationsEnabled
            prefs[badgeNotifsKey] = next.badgeNotificationsEnabled
            prefs[skillKey] = next.cookingSkill.name
        }
    }
}
