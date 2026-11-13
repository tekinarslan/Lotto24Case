package de.lotto24case

import androidx.compose.ui.window.ComposeUIViewController
import de.lotto24case.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}