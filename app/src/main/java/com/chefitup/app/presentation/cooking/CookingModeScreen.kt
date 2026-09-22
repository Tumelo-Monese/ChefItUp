package com.chefitup.app.presentation.cooking

import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chefitup.app.R
import com.chefitup.app.presentation.components.ChefItUpButton
import com.chefitup.app.presentation.components.ChefItUpLoading
import com.chefitup.app.presentation.components.ChefItUpOutlinedButton
import com.chefitup.app.presentation.components.ErrorState
import com.chefitup.app.presentation.components.TimerCard
import com.chefitup.app.presentation.theme.FlameOrange
import com.chefitup.app.util.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    onBack: () -> Unit,
    viewModel: CookingModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CookingModeEvent.XpEarned -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.cooking_xp_earned, event.xp)
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.cooking_mode_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::speakCurrentStep) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = stringResource(R.string.cooking_speak_step)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                ChefItUpLoading()
            }
            uiState.errorResId != null && uiState.recipe == null -> {
                ErrorState(
                    message = uiState.errorMessage ?: stringResource(uiState.errorResId!!),
                    onRetry = viewModel::load,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.steps.isEmpty() -> {
                Column(modifier = Modifier.padding(padding).padding(24.dp)) {
                    Text(stringResource(R.string.cooking_no_steps))
                    Spacer(modifier = Modifier.height(16.dp))
                    ChefItUpButton(
                        text = stringResource(R.string.cooking_finish),
                        onClick = {
                            viewModel.completeRecipe()
                            onBack()
                        }
                    )
                }
            }
            else -> {
                val step = uiState.steps[uiState.stepIndex]
                val progress = (uiState.stepIndex + 1f) / uiState.steps.size.toFloat()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.cooking_step_of,
                            uiState.stepIndex + 1,
                            uiState.steps.size
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = FlameOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = FlameOrange,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            (fadeIn() + slideInHorizontally { it / 6 }) togetherWith
                                (fadeOut() + slideOutHorizontally { -it / 6 })
                        },
                        label = "cook_step",
                        modifier = Modifier.weight(1f, fill = false)
                    ) { animatedStep ->
                        Column {
                            Text(
                                text = stringResource(R.string.recipe_step_number, animatedStep.number),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = animatedStep.step,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        stringResource(R.string.cooking_timer),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 30).forEach { minutes ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.startTimer(minutes) },
                                label = {
                                    Text(
                                        when (minutes) {
                                            5 -> stringResource(R.string.cooking_timer_5)
                                            10 -> stringResource(R.string.cooking_timer_10)
                                            15 -> stringResource(R.string.cooking_timer_15)
                                            else -> stringResource(R.string.cooking_timer_30)
                                        }
                                    )
                                }
                            )
                        }
                    }
                    if (uiState.timerTotalSeconds > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        TimerCard(
                            remainingSeconds = uiState.timerRemainingSeconds,
                            isRunning = uiState.timerRunning,
                            totalSeconds = uiState.timerTotalSeconds,
                            onPauseResume = viewModel::pauseOrResumeTimer,
                            onCancel = viewModel::cancelTimer
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ChefItUpOutlinedButton(
                            text = stringResource(R.string.cooking_previous),
                            onClick = viewModel::previousStep,
                            modifier = Modifier.weight(1f),
                            enabled = uiState.stepIndex > 0
                        )
                        if (uiState.stepIndex < uiState.steps.lastIndex) {
                            ChefItUpButton(
                                text = stringResource(R.string.cooking_next),
                                onClick = viewModel::nextStep,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            ChefItUpButton(
                                text = stringResource(R.string.cooking_finish),
                                onClick = {
                                    viewModel.completeRecipe()
                                    onBack()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
