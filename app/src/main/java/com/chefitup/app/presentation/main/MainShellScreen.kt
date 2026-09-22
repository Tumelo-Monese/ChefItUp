package com.chefitup.app.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chefitup.app.R
import com.chefitup.app.presentation.home.HomeScreen
import com.chefitup.app.presentation.mealplan.MealPlannerScreen
import com.chefitup.app.presentation.navigation.Screen
import com.chefitup.app.presentation.profile.ProfileScreen
import com.chefitup.app.presentation.saved.SavedScreen
import com.chefitup.app.presentation.search.SearchScreen

private data class TabItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainShellScreen(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val tabNavController = rememberNavController()
    val tabs = listOf(
        TabItem(Screen.Home, R.string.nav_home, Icons.Default.Home),
        TabItem(Screen.Search, R.string.nav_search, Icons.Default.Search),
        TabItem(Screen.MealPlanner, R.string.nav_meal_planner, Icons.Default.CalendarMonth),
        TabItem(Screen.Saved, R.string.nav_saved, Icons.Default.Bookmark),
        TabItem(Screen.Profile, R.string.nav_profile, Icons.Default.Person)
    )
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.screen.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenSearch = {
                        tabNavController.navigate(Screen.Search.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenCookWithIngredients = {
                        rootNavController.navigate(Screen.CookWithIngredients.route)
                    },
                    onOpenRecipe = { id ->
                        rootNavController.navigate(Screen.RecipeDetail.create(id))
                    },
                    onOpenMealPlanner = {
                        tabNavController.navigate(Screen.MealPlanner.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenSaved = {
                        tabNavController.navigate(Screen.Saved.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenProfile = {
                        tabNavController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onOpenRecipe = { id ->
                        rootNavController.navigate(Screen.RecipeDetail.create(id))
                    }
                )
            }
            composable(Screen.MealPlanner.route) {
                MealPlannerScreen(
                    onOpenRecipe = { id ->
                        rootNavController.navigate(Screen.RecipeDetail.create(id))
                    },
                    onOpenShoppingList = {
                        rootNavController.navigate(Screen.ShoppingList.route)
                    }
                )
            }
            composable(Screen.Saved.route) {
                SavedScreen(
                    onOpenRecipe = { id ->
                        rootNavController.navigate(Screen.RecipeDetail.create(id))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onOpenSettings = {
                        rootNavController.navigate(Screen.Settings.route)
                    },
                    onLoggedOut = onLogout
                )
            }
        }
    }
}
