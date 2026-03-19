package com.miguelrivera.vigiliafocus.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miguelrivera.vigiliafocus.domain.model.TimerSettings
import com.miguelrivera.vigiliafocus.presentation.theme.Dimens
import com.miguelrivera.vigiliafocus.presentation.theme.VigiliaTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vigiliafocus.composeapp.generated.resources.Res
import vigiliafocus.composeapp.generated.resources.action_back
import vigiliafocus.composeapp.generated.resources.settings_focus_duration
import vigiliafocus.composeapp.generated.resources.settings_long_break
import vigiliafocus.composeapp.generated.resources.settings_sessions_before_long
import vigiliafocus.composeapp.generated.resources.settings_short_break
import vigiliafocus.composeapp.generated.resources.settings_title

/**
 * Stateful wrapper for [SettingsLayout].
 * Extracts dependencies and state observation, leaving the UI completely decoupled.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        SettingsLayout(
            settings = uiState.settings,
            onSaveAndBack = { updatedSettings ->
                viewModel.saveSettings(updatedSettings)
                onNavigateBack()
            }
        )
    }
}

/**
 * Stateless UI component for Settings.
 * Fully decoupled from ViewModels for independent UI testing and previews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsLayout(
    settings: TimerSettings,
    onSaveAndBack: (TimerSettings) -> Unit
) {
    // Local state allows for fluid slider interactions without
    // waiting for round-trip persistence emissions.
    var localSettings by remember(settings) { mutableStateOf(settings) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    TextButton(onClick = { onSaveAndBack(localSettings) }) {
                        Text(stringResource(Res.string.action_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.SpacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLarge)
        ) {
            SettingSlider(
                label = stringResource(Res.string.settings_focus_duration),
                value = localSettings.focusDuration,
                range = 5f..60f,
                onValueChange = { localSettings = localSettings.copy(focusDuration = it) }
            )

            SettingSlider(
                label = stringResource(Res.string.settings_short_break),
                value = localSettings.shortBreakDuration,
                range = 1f..15f,
                onValueChange = { localSettings = localSettings.copy(shortBreakDuration = it) }
            )

            SettingSlider(
                label = stringResource(Res.string.settings_long_break),
                value = localSettings.longBreakDuration,
                range = 5f..30f,
                onValueChange = { localSettings = localSettings.copy(longBreakDuration = it) }
            )

            SettingSlider(
                label = stringResource(Res.string.settings_sessions_before_long),
                value = localSettings.sessionsBeforeLongBreak,
                range = 2f..8f,
                onValueChange = { localSettings = localSettings.copy(sessionsBeforeLongBreak = it) }
            )
        }
    }
}

/**
 * Custom slider component with label and current value display.
 */
@Composable
private fun SettingSlider(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "$value",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    VigiliaTheme {
        SettingsLayout(
            settings = TimerSettings(),
            onSaveAndBack = { }
        )
    }
}