package com.miguelrivera.vigiliafocus.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Defines the type-safe routing destinations for the Navigation graph.
 * Utilizing Kotlinx Serialization ensures compile-time safety for route arguments.
 */
@Serializable
data object TimerScreenRoute

@Serializable
data object SettingsScreenRoute