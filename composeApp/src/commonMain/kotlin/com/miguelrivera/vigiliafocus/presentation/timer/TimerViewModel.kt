package com.miguelrivera.vigiliafocus.presentation.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miguelrivera.vigiliafocus.domain.model.TimerState
import com.miguelrivera.vigiliafocus.domain.repository.ISettingsRepository
import com.miguelrivera.vigiliafocus.domain.usecase.PauseTimerUseCase
import com.miguelrivera.vigiliafocus.domain.usecase.ResetTimerUseCase
import com.miguelrivera.vigiliafocus.domain.usecase.SkipToNextModeUseCase
import com.miguelrivera.vigiliafocus.domain.usecase.StartTimerUseCase
import com.miguelrivera.vigiliafocus.platform.PlatformAlerter
import com.miguelrivera.vigiliafocus.platform.PlatformTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manages presentation logic and state orchestrations for the Pomodoro timer.
 *
 * Bridges domain use cases with the [PlatformTimer] implementation to drive state emissions.
 * Directly utilizes [TimerState] computed properties to maintain architectural consistency.
 */
class TimerViewModel(
    private val startTimerUseCase: StartTimerUseCase,
    private val pauseTimerUseCase: PauseTimerUseCase,
    private val resetTimerUseCase: ResetTimerUseCase,
    private val skipToNextModeUseCase: SkipToNextModeUseCase,
    private val settingsRepository: ISettingsRepository,
    private val platformTimer: PlatformTimer,
    private val platformAlerter: PlatformAlerter
) : ViewModel() {

    private val _timerState = MutableStateFlow(TimerState())

    init {
        syncSettings()
    }

    /**
     * Observes the settings repository and synchronizes updates into the domain state.
     *
     * If the timer is currently at the start of a session, changing durations will
     * automatically update the remaining seconds to match the new setting.
     */
    private fun syncSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings()
                .distinctUntilChanged()
                .collect { newSettings ->
                    _timerState.update { currentState ->
                        val stateWithNewSettings = currentState.copy(settings = newSettings)

                        if (currentState.isAtStart) {
                            // Snap to the new duration defined in the domain model if the timer is idle
                            resetTimerUseCase(stateWithNewSettings)
                        } else {
                            stateWithNewSettings
                        }
                    }
                }
        }
    }

    /**
     * UI State derived from the underlying domain state.
     *
     * Maps the raw [TimerState] into [TimerUiState] for the view layer, utilizing
     * the model's baseline duration for state evaluation.
     */
    val uiState: StateFlow<TimerUiState> = _timerState.map { state ->
        when {
            state.remainingSeconds <= 0 -> TimerUiState.Completed(state)
            state.isRunning -> TimerUiState.Running(state)
            state.remainingSeconds < state.totalSecondsForMode -> TimerUiState.Paused(state)
            else -> TimerUiState.Idle(state)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimerUiState.Idle(TimerState())
    )

    /**
     * Triggers the start of the timer countdown.
     */
    fun startTimer() {
        _timerState.update { startTimerUseCase(it) }

        platformTimer.start(
            durationSeconds = _timerState.value.remainingSeconds,
            onTick = { remaining ->
                _timerState.update { it.copy(remainingSeconds = remaining) }
            },
            onFinish = {
                handleSessionCompletion()
            }
        )
    }

    /**
     * Suspends the current timer countdown.
     */
    fun pauseTimer() {
        _timerState.update { pauseTimerUseCase(it) }
        platformTimer.pause()
    }

    /**
     * Resets the timer to the beginning of the current mode.
     */
    fun resetTimer() {
        _timerState.update { resetTimerUseCase(it) }
        platformTimer.reset()
    }

    /**
     * Forces a transition to the next mode in the Pomodoro cycle.
     */
    fun skipMode() {
        _timerState.update { skipToNextModeUseCase(it) }
        platformTimer.reset()
    }

    /**
     * Internal handler called when the [PlatformTimer] reaches zero.
     */
    private fun handleSessionCompletion() {
        platformAlerter.playCompletionAlert()
        platformTimer.reset()
        _timerState.update { skipToNextModeUseCase(it) }
    }
}