package com.chefitup.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String?,
    val readyInMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val rating: Double,
    val vegetarian: Boolean,
    val vegan: Boolean,
    val glutenFree: Boolean,
    val dairyFree: Boolean,
    val cuisinesJson: String,
    val dishTypesJson: String,
    val summary: String?,
    val ingredientsJson: String?,
    val instructionsJson: String?,
    val nutritionJson: String?,
    val cachedAtEpochMs: Long,
    val hasFullDetail: Boolean
)

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val recipeId: String,
    val title: String,
    val imageUrl: String?,
    val readyInMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val rating: Double,
    val vegetarian: Boolean,
    val vegan: Boolean,
    val glutenFree: Boolean,
    val dairyFree: Boolean,
    val cuisinesJson: String,
    val dishTypesJson: String,
    val savedAtEpochMs: Long
)

@Entity(tableName = "meal_plan")
data class MealPlanEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val recipeTitle: String,
    val recipeImageUrl: String?,
    val weekStartEpochDay: Long,
    val dayOfWeek: Int,
    val mealType: String,
    val servings: Int,
    val notes: String,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String,
    val isChecked: Boolean,
    val recipeId: String?,
    val isCustom: Boolean,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAtEpochMs: Long
)

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val payloadJson: String,
    val entityKey: String?,
    val createdAtEpochMs: Long,
    val attemptCount: Int = 0,
    val lastError: String? = null
)

@Entity(tableName = "gamification")
data class GamificationEntity(
    @PrimaryKey val id: Int = 1,
    val xp: Int,
    val recipesCompleted: Int,
    val earnedBadgesJson: String,
    val viewedRecipeIdsJson: String,
    val hasUsedMealPlan: Boolean,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String,
    val languageCode: String,
    val diet: String,
    val allergiesJson: String,
    val intolerancesJson: String,
    val preferredFoodsJson: String,
    val excludedIngredientsJson: String,
    val dislikedFoodsJson: String,
    val notificationsEnabled: Boolean,
    val mealRemindersEnabled: Boolean,
    val recipeRecommendationsEnabled: Boolean,
    val badgeNotificationsEnabled: Boolean,
    val cookingSkill: String
)

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey val recipeId: String,
    val title: String,
    val imageUrl: String?,
    val readyInMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val rating: Double,
    val vegetarian: Boolean,
    val vegan: Boolean,
    val glutenFree: Boolean,
    val dairyFree: Boolean,
    val cuisinesJson: String,
    val dishTypesJson: String,
    val viewedAtEpochMs: Long
)
