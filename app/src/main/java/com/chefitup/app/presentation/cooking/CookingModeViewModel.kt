package com.chefitup.app.presentation.cooking

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.chefitup.app.R
import com.chefitup.app.domain.gamification.GamificationRules
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.model.InstructionStep
import com.chefitup.app.domain.model.RecipeDetail
import com.chefitup.app.domain.repository.GamificationRepository
import com.chefitup.app.domain.repository.RecipeRepository
import com.chefitup.app.presentation.navigation.Screen
import com.chefitup.app.util.CookingTimerNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

data class CookingModeUiState(
    val recipe: RecipeDetail? = null,
    val steps: List<InstructionStep> = emptyList(),
    val stepIndex: Int = 0,
    val isLoading: Boolean = true,
    val errorResId: Int? = null,
    val errorMessage: String? = null,
    val timerTotalSeconds: Int = 0,
    val timerRemainingSeconds: Int = 0,
    val timerRunning: Boolean = false,
    val completed: Boolean = false
)

sealed class CookingModeEvent {
    data class XpEarned(val xp: Int) : CookingModeEvent()
}

@HiltViewModel
class CookingModeViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val gamificationRepository: GamificationRepository
) : AndroidViewModel(application) {

    private val recipeId: String = checkNotNull(savedStateHandle[Screen.CookingMode.ARG_RECIPE_ID])

    private val _uiState = MutableStateFlow(CookingModeUiState())
    val uiState: StateFlow<CookingModeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CookingModeEvent>()
    val events: SharedFlow<CookingModeEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var tts: TextToSpeech? = null

    init {
        CookingTimerNotifier.ensureChannel(application)
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorResId = null) }
            when (val outcome = recipeRepository.getDetail(recipeId)) {
                is ApiOutcome.Success -> {
                    val detail = outcome.data
                    _uiState.update {
                        it.copy(
                            recipe = detail,
                            steps = detail.instructions,
                            isLoading = false,
                            stepIndex = 0
                        )
                    }
                }
                is ApiOutcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorResId = outcome.messageResId ?: R.string.error_not_found,
                            errorMessage = outcome.message
                        )
                    }
                }
            }
        }
    }

    fun nextStep() {
        val state = _uiState.value
        if (state.stepIndex < state.steps.lastIndex) {
            _uiState.update { it.copy(stepIndex = it.stepIndex + 1) }
        }
    }

    fun previousStep() {
        if (_uiState.value.stepIndex > 0) {
            _uiState.update { it.copy(stepIndex = it.stepIndex - 1) }
        }
    }

    fun startTimer(minutes: Int) {
        val total = minutes * 60
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerTotalSeconds = total,
                timerRemainingSeconds = total,
                timerRunning = true
            )
        }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value
                if (!current.timerRunning) continue
                val next = current.timerRemainingSeconds - 1
                if (next <= 0) {
                    _uiState.update {
                        it.copy(timerRemainingSeconds = 0, timerRunning = false)
                    }
                    CookingTimerNotifier.notifyComplete(getApplication())
                    break
                } else {
                    _uiState.update { it.copy(timerRemainingSeconds = next) }
                }
            }
        }
    }

    fun pauseOrResumeTimer() {
        _uiState.update { it.copy(timerRunning = !it.timerRunning) }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(timerRunning = false, timerRemainingSeconds = 0, timerTotalSeconds = 0)
        }
    }

    fun speakCurrentStep() {
        val step = _uiState.value.steps.getOrNull(_uiState.value.stepIndex) ?: return
        tts?.speak(step.step, TextToSpeech.QUEUE_FLUSH, null, "step_${step.number}")
    }

    fun completeRecipe() {
        val recipe = _uiState.value.recipe ?: return
        if (_uiState.value.completed) return
        viewModelScope.launch {
            val weekend = LocalDate.now().dayOfWeek.let {
                it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY
            }
            val xp = GamificationRules.xpForCompletedRecipe(recipe.difficulty)
            when (
                gamificationRepository.completeRecipe(
                    recipeId = recipe.id,
                    difficulty = recipe.difficulty,
                    cookedOnWeekend = weekend
                )
            ) {
                is ApiOutcome.Success, is ApiOutcome.Failure -> {
                    _uiState.update { it.copy(completed = true) }
                    _events.emit(CookingModeEvent.XpEarned(xp))
                }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
