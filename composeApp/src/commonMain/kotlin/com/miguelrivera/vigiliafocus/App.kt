package com.miguelrivera.vigiliafocus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miguelrivera.vigiliafocus.presentation.navigation.SettingsScreenRoute
import com.miguelrivera.vigiliafocus.presentation.navigation.TimerScreenRoute
import com.miguelrivera.vigiliafocus.presentation.theme.VigiliaTheme
import com.miguelrivera.vigiliafocus.presentation.timer.TimerScreen

@Composable
fun App() {
    // KoinContext ensures that Koin's composition local is successfully propagated
    // throughout the multiplatform Compose tree.
    VigiliaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = TimerScreenRoute
            ) {
                composable<TimerScreenRoute> {
                    TimerScreen(
                        onNavigateToSettings = {
                            navController.navigate(SettingsScreenRoute)
                        }
                    )
                }

                composable<SettingsScreenRoute> {
                    /*SetingsScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        }
                    )*/
                }
            }
        }
    }
}