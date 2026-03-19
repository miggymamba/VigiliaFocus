package com.miguelrivera.vigiliafocus.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Initializes the Koin dependency container with the shared module graph.
 *
 * Each platform entry point calls this function once at application startup,
 * passing its own [platformModule] to satisfy platform-specific bindings
 * such as [ObservableSettings].
 *
 * The optional [configure] lambda lets platform callers apply target-specific
 * Koin configuration — for example, `androidContext(this)` on Android — without
 * polluting the shared initialization path.
 *
 * Added [configure] parameter so [VigiliaFocusApp] no longer needs to
 * call `startKoin` directly, keeping the initialization contract consistent
 * between Android and the future iOS entry point.
 *
 * @param platformModule A Koin module supplying platform-specific bindings.
 * @param configure Optional block for platform-specific Koin configuration.
 * @return The configured [KoinApplication] instance.
 */
fun initKoin(
    platformModule: Module,
    configure: KoinApplication.() -> Unit = {}
): KoinApplication = startKoin {
    configure()
    modules(sharedModules() + platformModule)
}