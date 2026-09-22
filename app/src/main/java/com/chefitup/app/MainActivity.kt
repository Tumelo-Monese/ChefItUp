package com.chefitup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.chefitup.app.domain.repository.UserPreferencesRepository
import com.chefitup.app.presentation.navigation.ChefItUpNavGraph
import com.chefitup.app.presentation.settings.ThemeViewModel
import com.chefitup.app.presentation.theme.ChefItUpTheme
import com.chefitup.app.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language = runBlocking {
            runCatching { userPreferencesRepository.getSnapshot().languageCode }.getOrDefault("en")
        }
        LocaleHelper.apply(this, language)

        enableEdgeToEdge()
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            ChefItUpTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    ChefItUpNavGraph(navController = navController)
                }
            }
        }
    }
}
