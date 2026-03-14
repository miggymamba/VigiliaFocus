package com.miguelrivera.vigiliafocus.data.repository

import com.miguelrivera.vigiliafocus.domain.model.TimerSettings
import com.miguelrivera.vigiliafocus.domain.repository.ISettingsRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * [ISettingsRepository] implementation backed by [multiplatform-settings].
 *
 * Wraps an [ObservableSettings] instance (SharedPreferences on Android,
 * NSUserDefaults on iOS) via [toFlowSettings], exposing each persisted
 * field as a reactive [Flow]. All four key flows are combined so that
 * [getSettings] re-emits a complete [TimerSettings] snapshot whenever
 * **any** individual field changes — not just the field whose key was
 * used to anchor the upstream flow.
 *
 * Keys and the datastore name are declared in [Companion] to keep
 * all persistence identifiers in one place and prevent typo-driven
 * data loss across saves and reads.
 *
 * @param observableSettings Platform-specific [ObservableSettings] instance
 *   injected by the Koin platform module. Must be backed by the same
 *   named store on every call — do not pass a new instance per read/write.
 */

@OptIn(ExperimentalSettingsApi::class)
class SettingsRepositoryImpl(observableSettings: ObservableSettings) : ISettingsRepository {

    private val flowSettings = observableSettings.toFlowSettings()

    companion object {
        const val STORE_NAME = "vigilia_focus_settings"
        private const val KEY_FOCUS_DURATION = "focus_duration"
        private const val KEY_SHORT_BREAK_DURATION = "short_break_duration"
        private const val KEY_LONG_BREAK_DURATION = "long_break_duration"
        private const val KEY_SESSIONS_BEFORE_LONG = "sessions_before_long_break"
    }

    /**
     * Returns a [Flow] that emits a complete [TimerSettings] snapshot on
     * collection and again whenever any persisted field changes.
     *
     * Implemented by combining the four individual key flows so that a
     * [saveSettings] call touching only one field still produces a full
     * emission with the latest values of all fields.
     *
     * Unset keys fall back to [TimerSettings] defaults, guaranteeing at
     * least one emission on first launch before any settings are saved.
     */
    override fun getSettings(): Flow<TimerSettings> {
        val defaults = TimerSettings()
        return combine(
            flowSettings.getIntFlow(KEY_FOCUS_DURATION, defaults.focusDuration),
            flowSettings.getIntFlow(KEY_SHORT_BREAK_DURATION, defaults.shortBreakDuration),
            flowSettings.getIntFlow(KEY_LONG_BREAK_DURATION, defaults.longBreakDuration),
            flowSettings.getIntFlow(KEY_SESSIONS_BEFORE_LONG, defaults.sessionsBeforeLongBreak)
        ) { focus, shortBreak, longBreak, sessions ->
            TimerSettings(
                focusDuration = focus,
                shortBreakDuration = shortBreak,
                longBreakDuration = longBreak,
                sessionsBeforeLongBreak = sessions
            )
        }
    }

    /**
     * Persists all four fields of [settings] atomically in sequence.
     *
     * Each [putInt] call triggers a downstream emission on its respective
     * key flow. Because [getSettings] combines all four flows, the final
     * combined emission reflects the fully updated [TimerSettings].
     *
     * Safe to call from any coroutine context — [FlowSettings.putInt]
     * is non-blocking.
     *
     * @param settings The [TimerSettings] to persist.
     */
    override suspend fun saveSettings(settings: TimerSettings) {
        flowSettings.putInt(KEY_FOCUS_DURATION, settings.focusDuration)
        flowSettings.putInt(KEY_SHORT_BREAK_DURATION, settings.shortBreakDuration)
        flowSettings.putInt(KEY_LONG_BREAK_DURATION, settings.longBreakDuration)
        flowSettings.putInt(KEY_SESSIONS_BEFORE_LONG, settings.sessionsBeforeLongBreak)
    }
}