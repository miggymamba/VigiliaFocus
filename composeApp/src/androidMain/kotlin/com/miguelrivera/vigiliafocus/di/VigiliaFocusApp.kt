package com.miguelrivera.vigiliafocus.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

/**
 * Application entry point for the Android target.
 *
 * Bootstraps the Koin dependency container with the shared module graph
 * and the Android-specific platform module. Called once by the Android
 * runtime before any Activity is created.
 */
class VigiliaFocusApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VigiliaFocusApp)
            modules(sharedModules() + androidModule)
        }
    }
}