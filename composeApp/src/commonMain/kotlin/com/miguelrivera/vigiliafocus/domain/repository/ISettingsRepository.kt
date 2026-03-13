package com.miguelrivera.vigiliafocus.domain.repository

import com.miguelrivera.vigiliafocus.domain.model.TimerSettings
import kotlinx.coroutines.flow.Flow

/**
 * Contract for reading and persisting user-configured timer settings.
 *
 * The interface lives in `commonMain` so that use cases and ViewModels
 * remain fully platform-agnostic. The Android implementation backed by
 * DataStore Preferences is provided in `androidMain` via
 * [DataStoreSettingsRepository] (VF3.S1).
 *
 * Implementations must guarantee:
 * - [getSettings] emits at least one value immediately upon collection,
 *   representing the last persisted state or the default [TimerSettings].
 * - [getSettings] remains active and emits on every subsequent save.
 * - [saveSettings] is safe to call from any coroutine context.
 *
 * @see TimerSettings
 */
interface ISettingsRepository {

    /**
     * Returns a cold [Flow] that emits the current [TimerSettings] and any
     * subsequent updates triggered by [saveSettings].
     *
     * @return A never-completing [Flow] of [TimerSettings].
     */
    fun getSettings(): Flow<TimerSettings>

    /**
     * Persists [settings], causing [getSettings] to emit the updated value.
     *
     * @param settings The [TimerSettings] to persist.
     */
    suspend fun saveSettings(settings: TimerSettings)
}