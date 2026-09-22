package com.chefitup.app.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chefitup.app.R
import com.chefitup.app.domain.model.ThemeMode
import com.chefitup.app.presentation.components.ChefItUpOutlinedButton
import com.chefitup.app.util.LocaleHelper
import com.chefitup.app.util.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = uiState.preferences
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.settings_dietary), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            val diets = listOf(
                "" to R.string.settings_diet_none,
                "vegetarian" to R.string.settings_diet_vegetarian,
                "vegan" to R.string.settings_diet_vegan,
                "gluten free" to R.string.settings_diet_gluten_free,
                "dairy free" to R.string.settings_diet_dairy_free
            )
            diets.forEach { (value, label) ->
                FilterChip(
                    selected = prefs.diet == value,
                    onClick = { viewModel.setDiet(value) },
                    label = { Text(stringResource(label)) },
                    modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = prefs.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                                    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                                    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                                }
                            )
                        },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            val languages = listOf(
                "en" to R.string.settings_language_en,
                "zu" to R.string.settings_language_zu,
                "af" to R.string.settings_language_af,
                "st" to R.string.settings_language_st
            )
            Row {
                languages.forEach { (code, label) ->
                    FilterChip(
                        selected = prefs.languageCode == code,
                        onClick = {
                            viewModel.setLanguage(code)
                            context.findActivity()?.let { activity ->
                                LocaleHelper.apply(activity, code)
                                activity.recreate()
                            }
                        },
                        label = { Text(stringResource(label)) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(R.string.settings_notifications), style = MaterialTheme.typography.titleMedium)
            SettingSwitch(
                label = stringResource(R.string.settings_notifications),
                checked = prefs.notificationsEnabled,
                onCheckedChange = viewModel::setNotificationsEnabled
            )
            SettingSwitch(
                label = stringResource(R.string.settings_meal_reminders),
                checked = prefs.mealRemindersEnabled,
                onCheckedChange = viewModel::setMealRemindersEnabled
            )
            SettingSwitch(
                label = stringResource(R.string.settings_recipe_recommendations),
                checked = prefs.recipeRecommendationsEnabled,
                onCheckedChange = viewModel::setRecipeRecommendationsEnabled
            )
            SettingSwitch(
                label = stringResource(R.string.settings_badge_notifications),
                checked = prefs.badgeNotificationsEnabled,
                onCheckedChange = viewModel::setBadgeNotificationsEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.settings_account), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            ChefItUpOutlinedButton(
                text = stringResource(R.string.profile_logout),
                onClick = { viewModel.logout(onLoggedOut) }
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
