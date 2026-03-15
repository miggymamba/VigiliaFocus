package com.miguelrivera.vigiliafocus.presentation.timer

import com.miguelrivera.vigiliafocus.domain.model.TimerState

/**
 * Represents the distinct interaction states of the Timer UI.
 */
sealed interface TimerUiState {
    val state: TimerState

    data class Idle(override val state: TimerState) : TimerUiState
    data class Running(override val state: TimerState) : TimerUiState
    data class Paused(override val state: TimerState) : TimerUiState
    data class Completed(override val state: TimerState) : TimerUiState
}