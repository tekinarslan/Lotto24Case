package de.lotto24case.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AuraColors.PrimaryContainer,
    onPrimary = AuraColors.OnPrimary,
    primaryContainer = AuraColors.JackpotGold,
    onPrimaryContainer = AuraColors.OnPrimaryContainer,
    secondary = AuraColors.Primary,
    onSecondary = AuraColors.OnPrimary,
    background = AuraColors.Background,
    onBackground = AuraColors.OnSurface,
    surface = AuraColors.Surface,
    onSurface = AuraColors.OnSurface,
    surfaceVariant = AuraColors.SurfaceContainerLow,
    onSurfaceVariant = AuraColors.OnSurfaceVariant,
    outline = AuraColors.Outline,
    outlineVariant = AuraColors.OutlineVariant,
    error = AuraColors.Error,
    onError = AuraColors.OnError
)

private val DarkColorScheme = darkColorScheme(
    primary = AuraColors.JackpotGold,
    onPrimary = AuraColors.OnSurface,
    primaryContainer = AuraColors.PrimaryContainer,
    onPrimaryContainer = AuraColors.OnPrimary,
    secondary = AuraColors.Primary,
    onSecondary = AuraColors.OnPrimary,
    background = AuraColors.DarkBackground,
    onBackground = AuraColors.DarkOnSurface,
    surface = AuraColors.DarkSurface,
    onSurface = AuraColors.DarkOnSurface,
    surfaceVariant = AuraColors.DarkSurfaceContainer,
    onSurfaceVariant = AuraColors.DarkOnSurfaceVariant,
    outline = AuraColors.Outline,
    outlineVariant = AuraColors.DarkOutlineVariant,
    error = AuraColors.Error,
    onError = AuraColors.OnError
)

@Composable
fun AuraLottoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography.materialTypography,
        content = content
    )
}
