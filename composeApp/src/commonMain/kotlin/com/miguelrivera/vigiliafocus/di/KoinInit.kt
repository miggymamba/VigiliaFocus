package com.miguelrivera.vigiliafocus.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Initializes the Koin dependency container with the shared module graph.
 *
 * Each platform entry point calls this function once at application startup,
 * passing its own [platformModule] to satisfy platform-specific bindings
 * such as [ISettingsRepository].
 *
 * @param platformModule A Koin module supplying platform-specific bindings.
 * @return The configured [KoinApplication] instance.
 */
fun initKoin(platformModule: Module): KoinApplication = startKoin {
    modules(sharedModules() + platformModule)
}