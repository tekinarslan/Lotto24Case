package de.lotto24case

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.lotto24case.presentation.theme.AuraLottoTheme
import de.lotto24case.presentation.ui.pullcord.PullCordColors
import de.lotto24case.presentation.ui.pullcord.PullCordLamp
import de.lotto24case.presentation.ui.pullcord.rememberPullCordState
import de.lotto24case.presentation.ui.screen.DrawResultsRoute

/**
 * Main cross-platform entry point Composable for Lotto24Case.
 *
 * Wraps the app in [PullCordLamp] interactive theme toggle and [AuraLottoTheme].
 */
@Composable
fun App() {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by rememberSaveable { mutableStateOf(systemDark) }

    val pullCordState = rememberPullCordState(lit = isDarkTheme)

    LaunchedEffect(isDarkTheme) {
        if (pullCordState.lit != isDarkTheme) {
            pullCordState.snapTo(isDarkTheme)
        }
    }

    PullCordLamp(
        state = pullCordState,
        across = 0.88f, // Positioned at top-right
        onSwitch = { lit ->
            isDarkTheme = lit
        },
        modifier = Modifier.fillMaxSize()
    ) { lit ->
        AuraLottoTheme(darkTheme = lit) {
            DrawResultsRoute(modifier = Modifier.fillMaxSize())
        }
    }
}
