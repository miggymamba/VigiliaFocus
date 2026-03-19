package com.miguelrivera.vigiliafocus.di

import android.content.Context
import com.miguelrivera.vigiliafocus.data.repository.SettingsRepositoryImpl
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module supplying Android-specific bindings.
 *
 * Provides the [ObservableSettings] instance backed by [SharedPreferencesSettings],
 * using the named store declared in [SettingsRepositoryImpl.STORE_NAME] to ensure
 * the Android backing store and the repository always reference the same file.
 *
 */
val androidModule = module {
    single<ObservableSettings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences(
                SettingsRepositoryImpl.STORE_NAME,
                Context.MODE_PRIVATE
            )
        )
    }
}