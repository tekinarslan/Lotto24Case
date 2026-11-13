package de.lotto24case.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Accessor object for custom Aura Lotto Design System tokens.
 */
object AuraTheme {
    val colors: AuraColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraColorScheme.current

    val spacing: AuraSpacing
        get() = AuraSpacing
}

private fun AuraColorScheme.toMaterialLight() = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = primary,
    onSecondary = onPrimary,
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceContainerLow,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    error = error,
    onError = onError
)

private fun AuraColorScheme.toMaterialDark() = darkColorScheme(
    primary = primary,
    onPrimary = onSurface,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = primary,
    onSecondary = onPrimary,
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceContainer,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    error = error,
    onError = onError
)

@Composable
fun AuraLottoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val auraColorScheme = if (darkTheme) AuraDarkColorScheme else AuraLightColorScheme
    val materialColorScheme = if (darkTheme) auraColorScheme.toMaterialDark() else auraColorScheme.toMaterialLight()

    CompositionLocalProvider(
        LocalAuraColorScheme provides auraColorScheme
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = AuraTypography.materialTypography,
            content = content
        )
    }
}
