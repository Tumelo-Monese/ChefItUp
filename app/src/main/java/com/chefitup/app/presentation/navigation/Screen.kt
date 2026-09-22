package com.chefitup.app.presentation.navigation

/**
 * Type-safe route names for Jetpack Navigation Compose.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")

    data object Main : Screen("main")

    data object Home : Screen("home")
    data object Search : Screen("search")
    data object MealPlanner : Screen("meal_planner")
    data object Saved : Screen("saved")
    data object Profile : Screen("profile")

    data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun create(recipeId: String) = "recipe_detail/$recipeId"
        const val ARG_RECIPE_ID = "recipeId"
    }

    data object CookWithIngredients : Screen("cook_with_ingredients")

    data object CookingMode : Screen("cooking_mode/{recipeId}") {
        fun create(recipeId: String) = "cooking_mode/$recipeId"
        const val ARG_RECIPE_ID = "recipeId"
    }

    data object Settings : Screen("settings")
    data object ShoppingList : Screen("shopping_list")
}
