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
 * Manages presentation logic and state orchestration for the Pomodoro timer.
 *
 * Bridges domain use cases with [PlatformTimer] to drive state emissions.
 * The internal [_timerState] is the single source of truth; [uiState] is a
 * derived mapping to [TimerUiState] consumed by the UI layer.
 *
 * ### Session completion flow
 *
 * When a countdown reaches zero, [handleSessionCompletion] plays the alert and
 * resets the platform timer, but deliberately does **not** advance the Pomodoro
 * cycle. The ViewModel emits [TimerUiState.Completed] and waits. The user must
 * explicitly tap SKIP — which calls [skipMode] — to advance to the next interval.
 *
 * This keeps the cycle under user control: a break never starts silently in the
 * background, and the completion banner is always visible for at least one
 * deliberate interaction.
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
     * If the timer is currently at the start of a session, changing durations
     * automatically snaps [TimerState.remainingSeconds] to the new configured value.
     * If the timer is mid-session the new settings propagate into the state so that
     * the next mode transition picks up the updated durations, but the live countdown
     * is left untouched.
     */
    private fun syncSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings()
                .distinctUntilChanged()
                .collect { newSettings ->
                    _timerState.update { currentState ->
                        val stateWithNewSettings = currentState.copy(settings = newSettings)
                        if (currentState.isAtStart) {
                            resetTimerUseCase(stateWithNewSettings)
                        } else {
                            stateWithNewSettings
                        }
                    }
                }
        }
    }

    /**
     * UI state derived from the underlying domain state.
     *
     * Maps the raw [TimerState] into [TimerUiState] for the view layer. The sealed
     * hierarchy lets [TimerScreen] route each variant to a dedicated composable
     * without inspecting raw [TimerState] fields.
     */
    val uiState: StateFlow<TimerUiState> = _timerState.map { state ->
        when {
            state.remainingSeconds <= 0 -> TimerUiState.Completed(state)
            state.isRunning            -> TimerUiState.Running(state)
            state.remainingSeconds < state.totalSecondsForMode -> TimerUiState.Paused(state)
            else                       -> TimerUiState.Idle(state)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimerUiState.Idle(TimerState())
    )

    /**
     * Starts the timer countdown from the current remaining time.
     *
     * Guards against double-start by checking [TimerState.isRunning] before
     * delegating to [PlatformTimer]. Without this guard a rapid double-tap on the
     * START button cancels the live tick job and resets [PlatformTimer.remainingSeconds]
     * back to the full duration mid-session.
     */
    fun startTimer() {
        // Idempotency guard — do nothing if the timer is already ticking.
        if (_timerState.value.isRunning) return

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
     * Advances the Pomodoro cycle to the next mode.
     *
     * Called both by the SKIP button during a live interval and by the SKIP button
     * on [TimerCompleteLayout] after a session ends. In both cases [SkipToNextModeUseCase]
     * determines the correct next [TimerMode] and resets the countdown.
     */
    fun skipMode() {
        _timerState.update { skipToNextModeUseCase(it) }
        platformTimer.reset()
    }

    /**
     * Internal handler called when [PlatformTimer] reaches zero.
     *
     * Plays the completion alert and resets the platform timer so it is ready for
     * the next interval. Deliberately does **not** call [skipToNextModeUseCase] —
     * [_timerState] is left with [TimerState.remainingSeconds] at 0, which causes
     * [uiState] to emit [TimerUiState.Completed]. The cycle only advances when the
     * user explicitly taps SKIP, which calls [skipMode].
     */
    private fun handleSessionCompletion() {
        platformAlerter.playCompletionAlert()
        platformTimer.reset()
        // Do not advance the cycle here. remainingSeconds stays at 0, surfacing
        // TimerUiState.Completed and giving the user a deliberate acknowledgement step.
    }
}