package com.miguelrivera.vigiliafocus.presentation.timer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miguelrivera.vigiliafocus.domain.model.TimerState
import com.miguelrivera.vigiliafocus.presentation.theme.Dimens
import com.miguelrivera.vigiliafocus.presentation.theme.VigiliaIcons
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vigiliafocus.composeapp.generated.resources.Res
import vigiliafocus.composeapp.generated.resources.action_pause
import vigiliafocus.composeapp.generated.resources.action_reset
import vigiliafocus.composeapp.generated.resources.action_skip
import vigiliafocus.composeapp.generated.resources.action_start
import vigiliafocus.composeapp.generated.resources.app_name
import vigiliafocus.composeapp.generated.resources.settings_title

/**
 * Stateful wrapper for [TimerScreen].
 * Extracts dependencies and state observation, leaving the UI completely decoupled.
 */
@Composable
fun TimerScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: TimerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimerLayout(
        state = uiState.state,
        onStart = viewModel::startTimer,
        onPause = viewModel::pauseTimer,
        onReset = viewModel::resetTimer,
        onSkip = viewModel::skipMode,
        onNavigateToSettings = onNavigateToSettings
    )
}

/**
 * Stateless UI component for the Pomodoro Timer.
 * Fully decoupled from ViewModels for independent UI testing and previews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerLayout(
    state: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = VigiliaIcons.Settings,
                            contentDescription = stringResource(Res.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SessionDots(
                completed = state.completedSessions,
                total = state.settings.sessionsBeforeLongBreak
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingHuge))

            TimerDial(
                remainingSeconds = state.remainingSeconds,
                totalSeconds = state.totalSecondsForMode
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingExtraLarge))

            Text(
                text = state.mode.name.replace("_", " "),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingHuge))

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)) {
                if (state.isRunning) {
                    Button(onClick = onPause) { Text(stringResource(Res.string.action_pause)) }
                } else {
                    Button(onClick = onStart) { Text(stringResource(Res.string.action_start)) }
                }

                OutlinedButton(onClick = onReset) { Text(stringResource(Res.string.action_reset)) }
                TextButton(onClick = onSkip) { Text(stringResource(Res.string.action_skip)) }
            }
        }
    }
}

/**
 * Visual representation of the remaining time using a circular progress indicator.
 *
 * @param remainingSeconds Seconds left in the current session.
 * @param totalSeconds Total duration of the current session mode.
 */
@Composable
private fun TimerDial(remainingSeconds: Int, totalSeconds: Int) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 1f
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeString = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(Dimens.TimerDialSize)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = Dimens.TimerStrokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = Dimens.TimerStrokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = timeString,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Medium
        )
    }
}


/**
 * Renders a row of indicators representing completed and remaining focus sessions.
 *
 * @param completed Number of focus sessions completed in the current cycle.
 * @param total Total number of sessions required before a long break.
 */
@Composable
private fun SessionDots(completed: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
        repeat(total) { index ->
            Surface(
                modifier = Modifier.size(Dimens.SessionDotSize),
                shape = CircleShape,
                color = if (index < completed) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = BorderStroke(Dimens.BorderStrokeThin, MaterialTheme.colorScheme.primary)
            ) { }
        }
    }
}


/**
 * Preview provider for [TimerLayout] to verify UI states during development.
 */
@Preview
@Composable
private fun TimerLayoutPreview() {
    MaterialTheme {
        TimerLayout(
            state = TimerState(),
            onStart = {},
            onPause = {},
            onReset = {},
            onSkip = {},
            onNavigateToSettings = {}
        )
    }
}