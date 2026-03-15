package com.miguelrivera.vigiliafocus.presentation.settings

import com.miguelrivera.vigiliafocus.domain.model.TimerSettings

/**
 * Represents the UI state for the settings screen.
 */
data class SettingsUiState(
    val settings: TimerSettings = TimerSettings(),
    val isLoading: Boolean = false
)