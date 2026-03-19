package com.miguelrivera.vigiliafocus.presentation.theme

import androidx.compose.ui.graphics.Color

// Vigilia Brand Palette
// Design System: "Monochromatic Focus"

// Brand Primaries
val FocusBlack = Color(0xFF121212)
val FocusWhite = Color(0xFFFFFFFF)
val FocusGreyLight = Color(0xFFE0E0E0)
val FocusGreyDark = Color(0xFF555555)

// Neutrals
val SurfaceDark = Color(0xFF1E1E1E)
val BackgroundLight = Color(0xFFF5F5F5)
val DividerGrey = Color(0xFF333333)

// Functional
val AlertRed = Color(0xFFBA1A1A)
val SoftRed = Color(0xFFFFB4AB)
val DarkRed = Color(0xFF690005)

// --- Material 3 Mapping ---

// Light Colors
val md_theme_light_primary = FocusBlack
val md_theme_light_onPrimary = FocusWhite
val md_theme_light_primaryContainer = FocusGreyLight
val md_theme_light_onPrimaryContainer = FocusBlack
val md_theme_light_secondary = FocusGreyDark
val md_theme_light_onSecondary = FocusWhite
val md_theme_light_error = AlertRed
val md_theme_light_onError = FocusWhite
val md_theme_light_background = BackgroundLight
val md_theme_light_onBackground = FocusBlack
val md_theme_light_surface = FocusWhite
val md_theme_light_onSurface = FocusBlack
val md_theme_light_surfaceVariant = FocusGreyLight
val md_theme_light_onSurfaceVariant = FocusGreyDark

// Dark Colors
val md_theme_dark_primary = FocusGreyLight
val md_theme_dark_onPrimary = FocusBlack
val md_theme_dark_primaryContainer = FocusGreyDark
val md_theme_dark_onPrimaryContainer = FocusWhite
val md_theme_dark_secondary = FocusGreyLight
val md_theme_dark_onSecondary = FocusBlack
val md_theme_dark_error = SoftRed
val md_theme_dark_onError = DarkRed
val md_theme_dark_background = FocusBlack
val md_theme_dark_onBackground = FocusWhite
val md_theme_dark_surface = FocusBlack
val md_theme_dark_onSurface = FocusWhite

val md_theme_dark_surfaceVariant = DividerGrey
val md_theme_dark_onSurfaceVariant = FocusGreyLight