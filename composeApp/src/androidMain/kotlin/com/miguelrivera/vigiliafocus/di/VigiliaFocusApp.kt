package com.miguelrivera.vigiliafocus.di

import android.app.Application
import org.koin.android.ext.koin.androidContext

/**
 * Application entry point for the Android target.
 *
 * Bootstraps the Koin dependency container via [initKoin], the shared entry
 * point defined in `commonMain`, passing the Android-specific [androidModule]
 * as the platform module. This mirrors the pattern iOS will use in Phase 6,
 * keeping the initialization contract consistent across targets.
 *
 */
class VigiliaFocusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(platformModule = androidModule) {
            androidContext(this@VigiliaFocusApp)
        }
    }
}