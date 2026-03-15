package com.miguelrivera.vigiliafocus.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miguelrivera.vigiliafocus.domain.model.TimerSettings
import com.miguelrivera.vigiliafocus.domain.repository.ISettingsRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages user preferences for timer durations and cycle lengths.
 *
 * Observes the settings repository for persistence changes and provides a
 * synchronized state for the settings interface.
 */
class SettingsViewModel(private val settingsRepository: ISettingsRepository) : ViewModel() {

    /**
     * Exposes the current settings state to the UI.
     *
     * Uses distinctUntilChanged to mitigate the sequential emission spam caused by
     * the underlying multiplatform-settings sequential write pattern.
     */
    val uiState: StateFlow<SettingsUiState> = settingsRepository.getSettings()
        .distinctUntilChanged()
        .map { settings -> SettingsUiState(settings = settings, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(isLoading = true)
        )

    /**
     * Persists updated settings to the underlying platform storage.
     *
     * The save operation is wrapped in [NonCancellable] to ensure that sequential
     * writes to multiple settings keys are completed even if the user navigates
     * away and the ViewModel scope is canceled.
     */
    fun saveSettings(updatedSettings: TimerSettings) {
        viewModelScope.launch {
            // Protect against partial data corruption if the scope is canceled during back-nav
            withContext(NonCancellable) {
                settingsRepository.saveSettings(updatedSettings)
            }

        }
    }
}