package com.chefitup.app.domain.model

data class RecipeSummary(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val readyInMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val rating: Double,
    val vegetarian: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val dairyFree: Boolean = false,
    val cuisines: List<String> = emptyList(),
    val dishTypes: List<String> = emptyList(),
    val usedIngredientCount: Int? = null,
    val missedIngredientCount: Int? = null,
    val matchedIngredientsLabel: String? = null
)

data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String,
    val original: String = ""
)

data class InstructionStep(
    val number: Int,
    val step: String
)

data class NutritionItem(
    val name: String,
    val amount: Double,
    val unit: String
)

data class RecipeDetail(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val readyInMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val rating: Double,
    val vegetarian: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val dairyFree: Boolean = false,
    val cuisines: List<String> = emptyList(),
    val dishTypes: List<String> = emptyList(),
    val summary: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<InstructionStep> = emptyList(),
    val nutrition: List<NutritionItem> = emptyList()
) {
    fun toSummary(): RecipeSummary = RecipeSummary(
        id = id,
        title = title,
        imageUrl = imageUrl,
        readyInMinutes = readyInMinutes,
        servings = servings,
        difficulty = difficulty,
        rating = rating,
        vegetarian = vegetarian,
        vegan = vegan,
        glutenFree = glutenFree,
        dairyFree = dairyFree,
        cuisines = cuisines,
        dishTypes = dishTypes
    )
}

data class RecipeFilters(
    val query: String = "",
    val cuisine: String? = null,
    val diet: String? = null,
    val type: String? = null,
    val maxReadyTime: Int? = null,
    val vegetarian: Boolean? = null,
    val vegan: Boolean? = null,
    val glutenFree: Boolean? = null,
    val dairyFree: Boolean? = null,
    val offset: Int = 0,
    val number: Int = 20
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}

enum class ChefLevel {
    HOME_COOK,
    SOUS_CHEF,
    MASTER_CHEF,
    AWARD_WINNING_CHEF;

    val displayName: String
        get() = when (this) {
            HOME_COOK -> "Home Cook"
            SOUS_CHEF -> "Sous Chef"
            MASTER_CHEF -> "Master Chef"
            AWARD_WINNING_CHEF -> "Award-Winning Chef"
        }
}

data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String
)

data class UserGamification(
    val xp: Int = 0,
    val level: ChefLevel = ChefLevel.HOME_COOK,
    val recipesCompleted: Int = 0,
    val earnedBadgeIds: List<String> = emptyList(),
    val xpInCurrentLevel: Int = 0,
    val xpNeededForNextLevel: Int = 500
)

data class MealPlanEntry(
    val id: String,
    val recipeId: String,
    val recipeTitle: String,
    val recipeImageUrl: String?,
    val weekStartEpochDay: Long,
    val dayOfWeek: Int,
    val mealType: MealType,
    val servings: Int = 1,
    val notes: String = ""
)

data class ShoppingListItem(
    val id: String,
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String = "Pantry",
    val isChecked: Boolean = false,
    val recipeId: String? = null,
    val isCustom: Boolean = false
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class CookingSkill {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageCode: String = "en",
    val diet: String = "",
    val allergies: List<String> = emptyList(),
    val intolerances: List<String> = emptyList(),
    val preferredFoods: List<String> = emptyList(),
    val excludedIngredients: List<String> = emptyList(),
    val dislikedFoods: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val mealRemindersEnabled: Boolean = true,
    val recipeRecommendationsEnabled: Boolean = true,
    val badgeNotificationsEnabled: Boolean = true,
    val cookingSkill: CookingSkill = CookingSkill.BEGINNER
)
