package de.lotto24case.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic color scheme for Aura Lotto design system.
 * Serves as the Single Source of Truth for both Material theme mapping and custom lottery domain UI tokens.
 */
@Immutable
data class AuraColorScheme(
    // Canvas & Surfaces
    val background: Color,
    val surface: Color,
    val surfaceBorder: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,

    // Typography & Ink
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val onSurfaceSubtle: Color,

    // Brand & Lottery Accents
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val outline: Color,
    val outlineVariant: Color,
    val jackpotGold: Color,
    val jackpotGoldDark: Color,

    // Anchor Strip (Left vertical banner)
    val anchorLottoGradient: List<Color>,
    val anchorEuroGradient: List<Color>,
    val onAnchorTitle: Color,
    val onAnchorSubtitle: Color,
    val onAnchorUnit: Color,

    // Ball Specifics
    val ballRegularBackground: Color,
    val ballRegularHighlight: Color,
    val ballRegularShadow: Color,
    val ballRegularText: Color,
    val ballRegularBorder: Color,
    val superzahlRuby: Color,
    val superzahlRubyHighlight: Color,
    val superzahlRubyShadow: Color,
    val superzahlPodBackground: Color,
    val superzahlPodBorder: Color,
    val eurozahlAmber: Color,
    val eurozahlAmberHighlight: Color,
    val eurozahlAmberShadow: Color,
    val eurozahlPodBackground: Color,
    val eurozahlPodBorder: Color,

    // Supplementary Games
    val supplementary77Number: Color,
    val supplementarySuper6Number: Color,

    // Status & Badges
    val remainingTimeBackground: Color,
    val remainingTimeBorder: Color,
    val remainingTimeText: Color,

    val nextDraw6aus49Background: Color,
    val nextDraw6aus49Border: Color,
    val nextDraw6aus49Label: Color,
    val nextDraw6aus49Date: Color,

    val nextDrawEuroBackground: Color,
    val nextDrawEuroBorder: Color,
    val nextDrawEuroLabel: Color,
    val nextDrawEuroDate: Color,

    // Banners & Feedback
    val infoBannerBackground: Color,
    val infoBannerBorder: Color,
    val infoBannerText: Color,
    val offlineBannerBackground: Color,
    val offlineBannerBorder: Color,
    val offlineBannerText: Color,
    val error: Color,
    val onError: Color
)

val AuraLightColorScheme = AuraColorScheme(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceBorder = Color(0xFFE2E8F0),
    surfaceContainerLow = Color(0xFFF1F5F9),
    surfaceContainer = Color(0xFFEAEDFF),
    surfaceContainerHigh = Color(0xFFE2E8F0),

    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF334155),
    onSurfaceSubtle = Color(0xFF64748B),

    primary = Color(0xFFCA8A04),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEAB308),
    onPrimaryContainer = Color(0xFF422A00),
    outline = Color(0xFF837562),
    outlineVariant = Color(0xFFE2E8F0),
    jackpotGold = Color(0xFFEAB308),
    jackpotGoldDark = Color(0xFFCA8A04),

    anchorLottoGradient = listOf(Color(0xFFEAB308), Color(0xFFCA8A04), Color(0xFFA16207)),
    anchorEuroGradient = listOf(Color(0xFFCA8A04), Color(0xFFB45309), Color(0xFF78350F)),
    onAnchorTitle = Color.White,
    onAnchorSubtitle = Color(0xFFFEF3C7),
    onAnchorUnit = Color(0xFFFDE68A),

    ballRegularBackground = Color(0xFFF8FAFC),
    ballRegularHighlight = Color(0xFFFFFFFF),
    ballRegularShadow = Color(0xFFCBD5E1),
    ballRegularText = Color(0xFF0F172A),
    ballRegularBorder = Color(0xFFCBD5E1),
    superzahlRuby = Color(0xFFE11D48),
    superzahlRubyHighlight = Color(0xFFFF8599),
    superzahlRubyShadow = Color(0xFF9F1239),
    superzahlPodBackground = Color(0xFFFDE8E8),
    superzahlPodBorder = Color(0xFFFCA5A5),
    eurozahlAmber = Color(0xFFCA8A04),
    eurozahlAmberHighlight = Color(0xFFFDE68A),
    eurozahlAmberShadow = Color(0xFF854D0E),
    eurozahlPodBackground = Color(0xFFFEF3C7),
    eurozahlPodBorder = Color(0xFFFDE68A),

    supplementary77Number = Color(0xFFCA8A04),
    supplementarySuper6Number = Color(0xFFE11D48),

    remainingTimeBackground = Color(0xFFFEF3C7),
    remainingTimeBorder = Color(0xFFFDE68A),
    remainingTimeText = Color(0xFF92400E),

    nextDraw6aus49Background = Color(0xFFCA8A04),
    nextDraw6aus49Border = Color(0xFFD97706),
    nextDraw6aus49Label = Color(0xFFFEF3C7),
    nextDraw6aus49Date = Color(0xFFFFFFFF),

    nextDrawEuroBackground = Color(0xFF451A03),
    nextDrawEuroBorder = Color(0xFF78350F),
    nextDrawEuroLabel = Color(0xFFFDE68A),
    nextDrawEuroDate = Color(0xFFF59E0B),

    infoBannerBackground = Color(0xFFFEF3C7),
    infoBannerBorder = Color(0xFFFDE68A),
    infoBannerText = Color(0xFF92400E),
    offlineBannerBackground = Color(0xFFFEF2F2),
    offlineBannerBorder = Color(0xFFFECACA),
    offlineBannerText = Color(0xFF991B1B),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

val AuraDarkColorScheme = AuraColorScheme(
    background = Color(0xFF090D16),
    surface = Color(0xFF131B2E),
    surfaceBorder = Color(0xFF334155),
    surfaceContainerLow = Color(0xFF0F172A),
    surfaceContainer = Color(0xFF1E293B),
    surfaceContainerHigh = Color(0xFF334155),

    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    onSurfaceSubtle = Color(0xFF64748B),

    primary = Color(0xFFCA8A04),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCA8A04),
    onPrimaryContainer = Color(0xFFFEF3C7),
    outline = Color(0xFF837562),
    outlineVariant = Color(0xFF334155),
    jackpotGold = Color(0xFFF59E0B),
    jackpotGoldDark = Color(0xFFCA8A04),

    anchorLottoGradient = listOf(Color(0xFFB45309), Color(0xFF854D0E), Color(0xFF451A03)),
    anchorEuroGradient = listOf(Color(0xFF78350F), Color(0xFF451A03), Color(0xFF2E1004)),
    onAnchorTitle = Color.White,
    onAnchorSubtitle = Color(0xFFFEF3C7),
    onAnchorUnit = Color(0xFFFDE68A),

    ballRegularBackground = Color(0xFF1E293B),
    ballRegularHighlight = Color(0xFF334155),
    ballRegularShadow = Color(0xFF0F172A),
    ballRegularText = Color(0xFFF8FAFC),
    ballRegularBorder = Color(0xFF334155),
    superzahlRuby = Color(0xFFE11D48),
    superzahlRubyHighlight = Color(0xFFFB7185),
    superzahlRubyShadow = Color(0xFF881337),
    superzahlPodBackground = Color(0xFF1E293B),
    superzahlPodBorder = Color(0xFF334155),
    eurozahlAmber = Color(0xFFCA8A04),
    eurozahlAmberHighlight = Color(0xFFFBBF24),
    eurozahlAmberShadow = Color(0xFF78350F),
    eurozahlPodBackground = Color(0xFF1E293B),
    eurozahlPodBorder = Color(0xFF334155),

    supplementary77Number = Color(0xFFF59E0B),
    supplementarySuper6Number = Color(0xFFFB7185),

    remainingTimeBackground = Color(0xFF1E293B),
    remainingTimeBorder = Color(0xFF334155),
    remainingTimeText = Color(0xFFFDE68A),

    nextDraw6aus49Background = Color(0xFF854D0E),
    nextDraw6aus49Border = Color(0xFFA16207),
    nextDraw6aus49Label = Color(0xFFFEF3C7),
    nextDraw6aus49Date = Color(0xFFFFFFFF),

    nextDrawEuroBackground = Color(0xFF2E1004),
    nextDrawEuroBorder = Color(0xFF78350F),
    nextDrawEuroLabel = Color(0xFFFDE68A),
    nextDrawEuroDate = Color(0xFFF59E0B),

    infoBannerBackground = Color(0xFF1E293B),
    infoBannerBorder = Color(0xFF334155),
    infoBannerText = Color(0xFFFDE68A),
    offlineBannerBackground = Color(0xFF450A0A),
    offlineBannerBorder = Color(0xFF7F1D1D),
    offlineBannerText = Color(0xFFFCA5A5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

val LocalAuraColorScheme = staticCompositionLocalOf { AuraLightColorScheme }
