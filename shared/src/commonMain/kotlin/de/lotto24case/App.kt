package de.lotto24case

import androidx.compose.runtime.Composable
import de.lotto24case.presentation.theme.AuraLottoTheme
import de.lotto24case.presentation.ui.screen.DrawResultsRoute

/**
 * Main cross-platform entry point Composable for Lotto24Case.
 *
 * Wraps the app in [AuraLottoTheme] and navigates to [DrawResultsRoute].
 */
@Composable
fun App() {
    AuraLottoTheme {
        DrawResultsRoute()
    }
}
