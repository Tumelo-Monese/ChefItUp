package com.chefitup.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chefitup.app.presentation.auth.login.LoginScreen
import com.chefitup.app.presentation.auth.register.RegisterScreen
import com.chefitup.app.presentation.cook.CookWithIngredientsScreen
import com.chefitup.app.presentation.cooking.CookingModeScreen
import com.chefitup.app.presentation.main.MainShellScreen
import com.chefitup.app.presentation.onboarding.OnboardingScreen
import com.chefitup.app.presentation.recipe.RecipeDetailScreen
import com.chefitup.app.presentation.settings.SettingsScreen
import com.chefitup.app.presentation.shopping.ShoppingListScreen
import com.chefitup.app.presentation.splash.SplashScreen
import com.chefitup.app.presentation.theme.ChefMotion

@Composable
fun ChefItUpNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            fadeIn(tween(ChefMotion.Medium)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(ChefMotion.Medium)
            )
        },
        exitTransition = {
            fadeOut(tween(ChefMotion.Fast))
        },
        popEnterTransition = {
            fadeIn(tween(ChefMotion.Medium))
        },
        popExitTransition = {
            fadeOut(tween(ChefMotion.Fast)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(ChefMotion.Medium)
            )
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Main.route) {
            MainShellScreen(
                rootNavController = navController,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument(Screen.RecipeDetail.ARG_RECIPE_ID) { type = NavType.StringType })
        ) {
            RecipeDetailScreen(
                onBack = { navController.popBackStack() },
                onStartCooking = { recipeId ->
                    navController.navigate(Screen.CookingMode.create(recipeId))
                }
            )
        }

        composable(Screen.CookWithIngredients.route) {
            CookWithIngredientsScreen(
                onBack = { navController.popBackStack() },
                onOpenRecipe = { id ->
                    navController.navigate(Screen.RecipeDetail.create(id))
                }
            )
        }

        composable(
            route = Screen.CookingMode.route,
            arguments = listOf(navArgument(Screen.CookingMode.ARG_RECIPE_ID) { type = NavType.StringType })
        ) {
            CookingModeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
