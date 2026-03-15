package com.miguelrivera.vigiliafocus.di

import com.miguelrivera.vigiliafocus.data.repository.SettingsRepositoryImpl
import com.miguelrivera.vigiliafocus.domain.repository.ISettingsRepository
import com.miguelrivera.vigiliafocus.domain.usecase.PauseTimerUseCase
import com.miguelrivera.vigiliafocus.domain.usecase.ResetTimerUseCase
import com.miguelrivera.vigiliafocus.domain.usecase.SkipToNextModeUseCase
import com.miguelrivera.vigiliafocus.domain.usecase.StartTimerUseCase
import com.miguelrivera.vigiliafocus.platform.PlatformTimer
import com.miguelrivera.vigiliafocus.presentation.settings.SettingsViewModel
import com.miguelrivera.vigiliafocus.presentation.timer.TimerViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Root Koin module for the shared application graph.
 *
 * Platform-specific modules are injected at startup via [platformModule]
 * and merged here to form the complete dependency graph. Each feature
 * adds its own module as the presentation layer grows in Phase 4.
 */
val appModule: Module = module {
    single<ISettingsRepository> { SettingsRepositoryImpl(observableSettings = get()) }

    // Domain Use Cases required by TimerViewModel
    singleOf(::StartTimerUseCase)
    singleOf(::PauseTimerUseCase)
    singleOf(::ResetTimerUseCase)
    singleOf(::SkipToNextModeUseCase)

    // Platform dependencies (expect/actual)
    singleOf(::PlatformTimer)

    // VF4.S1: TimerViewModel registered here
    viewModelOf(::TimerViewModel)

    // VF4.S2: SettingsViewModel registered here
    viewModelOf(::SettingsViewModel)
}

/**
 * Aggregates all shared modules into a single list for platform entry points.
 *
 * Platform-specific entry points pass [platformModule] alongside this list
 * when calling [startKoin].
 */
fun sharedModules(): List<Module> = listOf(appModule)