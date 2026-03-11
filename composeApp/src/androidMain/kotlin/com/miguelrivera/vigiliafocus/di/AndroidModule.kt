package com.miguelrivera.vigiliafocus.di

import org.koin.dsl.module

/**
 * Koin module supplying Android-specific bindings.
 *
 * Provides the Android actual for [ISettingsRepository] via
 * [DataStoreSettingsRepository], implemented in VF3.S1.
 */
var androidModule = module {
    // VF3.S2: DataStoreSettingsRepository bound to ISettingsRepository here
}