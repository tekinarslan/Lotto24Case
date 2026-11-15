package de.lotto24case.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider as GlanceColorProvider
import de.lotto24case.MainActivity
import de.lotto24case.widget.model.WidgetDrawState
import de.lotto24case.widget.model.WidgetSupplementaryGame
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class Lotto24GlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetStates: ImmutableList<WidgetDrawState> = try {
            Lotto24WidgetHelper.getWidgetDrawData(forceRefresh = false)
                .ifEmpty { sampleWidgetStates() }
                .toImmutableList()
        } catch (_: Exception) {
            sampleWidgetStates()
        }

        provideContent {
            GlanceTheme {
                Lotto24DualCardWidgetContent(states = widgetStates)
            }
        }
    }

    companion object {
        fun sampleWidgetStates(): ImmutableList<WidgetDrawState> = persistentListOf(
            WidgetDrawState(
                lotteryId = "6aus49",
                title = "LOTTO 6aus49",
                formattedDate = "Sa., 21.01.2023",
                nextDrawText = "Nächste Ziehung: Mi., 25.01.2023",
                jackpotAmountValue = "14",
                jackpotUnitValue = "MIO",
                jackpotFormatted = "14 MIO €",
                mainBallsLabel = "GEWINNZAHLEN",
                specialBallLabel = "Superzahl: 7",
                regularNumbers = persistentListOf(4, 19, 24, 43, 44, 45),
                specialNumbers = persistentListOf(7),
                supplementaryGames = persistentListOf(
                    WidgetSupplementaryGame(name = "Spiel 77", winningNumber = "9415812"),
                    WidgetSupplementaryGame(name = "SUPER6", winningNumber = "381144")
                ),
                anchorLabel = "6AUS49",
                isFixture = false,
                specialNumberLabel = "Superzahl"
            ),
            WidgetDrawState(
                lotteryId = "eurojackpot",
                title = "Eurojackpot",
                formattedDate = "Fr., 20.01.2023",
                nextDrawText = "Nächste Ziehung: Di., 24.01.2023",
                jackpotAmountValue = "45",
                jackpotUnitValue = "MIO",
                jackpotFormatted = "45 MIO €",
                mainBallsLabel = "5 GEWINNZAHLEN",
                specialBallLabel = "2 EUROZAHLEN",
                regularNumbers = persistentListOf(3, 12, 18, 25, 41),
                specialNumbers = persistentListOf(2, 7),
                supplementaryGames = persistentListOf(),
                anchorLabel = "EURO",
                isFixture = false,
                specialNumberLabel = "Eurozahlen"
            )
        )
    }
}

private fun glanceColor(
    light: Color,
    dark: Color = light
): GlanceColorProvider = ColorProvider(
    day = light,
    night = dark
)

@Composable
private fun Lotto24DualCardWidgetContent(
    states: ImmutableList<WidgetDrawState>
) {
    val glanceSize = LocalSize.current
    val isLarge = glanceSize.height >= 180.dp || glanceSize.height == 0.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        states.forEachIndexed { index, drawState ->
            GlanceHomeStyleCard(
                drawState = drawState,
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxWidth(),
                isLarge = isLarge,
                widgetWidth = glanceSize.width
            )
            if (index < states.size - 1) {
                Spacer(modifier = GlanceModifier.height(if (isLarge) 10.dp else 6.dp))
            }
        }
    }
}

@Composable
private fun GlanceHomeStyleCard(
    drawState: WidgetDrawState,
    modifier: GlanceModifier = GlanceModifier,
    isLarge: Boolean = true,
    widgetWidth: Dp = 320.dp
) {
    val isLotto6aus49 = drawState.lotteryId.contains("6aus49", ignoreCase = true)

    // AuraTheme Semantic Color Equivalents
    val surfaceColor = glanceColor(light = Color(0xFFFFFFFF), dark = Color(0xFF0F172A))
    val surfaceBorderColor = glanceColor(light = Color(0xFFE2E8F0), dark = Color(0xFF1E293B))
    val surfaceContainerColor = glanceColor(light = Color(0xFFF8FAFC), dark = Color(0xFF1E293B))
    val surfaceContainerLowColor = glanceColor(light = Color(0xFFF1F5F9), dark = Color(0xFF020617))

    val onSurfaceColor = glanceColor(light = Color(0xFF0F172A), dark = Color(0xFFF8FAFC))
    val onSurfaceVariantColor = glanceColor(light = Color(0xFF475569), dark = Color(0xFFCBD5E1))
    val onSurfaceSubtleColor = glanceColor(light = Color(0xFF64748B), dark = Color(0xFF94A3B8))
    val primaryColor = glanceColor(light = Color(0xFFD97706), dark = Color(0xFFFBBF24))

    // Anchor Strip Colors
    val anchorBgColor = if (isLotto6aus49) {
        glanceColor(light = Color(0xFFE5A91E), dark = Color(0xFFD97706))
    } else {
        glanceColor(light = Color(0xFFB45309), dark = Color(0xFF78350F))
    }
    val onAnchorTitle = glanceColor(Color.White)
    val onAnchorSubtitle = glanceColor(Color(0xFFFEF3C7))
    val onAnchorUnit = glanceColor(Color(0xFFFDE68A))

    // ==================== BALL GEOMETRY (GLANCE <= 10 CHILDREN SAFE) ====================
    val effectiveWidth = if (widgetWidth > 0.dp) (widgetWidth - 12.dp) else 300.dp
    val anchorWidth = if (isLarge) 56.dp else 46.dp
    val bodyHorizontalPadding = if (isLarge) 24.dp else 16.dp
    val trayHorizontalPadding = 12.dp
    val outerAndTrayBorders = 4.dp

    val availableTrayWidth = (
            effectiveWidth - anchorWidth - bodyHorizontalPadding - trayHorizontalPadding - outerAndTrayBorders
            ).coerceAtLeast(150.dp)

    val regularCount = drawState.regularNumbers.size.coerceAtLeast(1)
    val specialCount = drawState.specialNumbers.size
    val totalBalls = regularCount + specialCount

    val isWide = effectiveWidth >= 340.dp
    val regularGap = when {
        !isLarge -> 0.dp
        isWide -> 2.dp
        else -> 2.dp
    }
    val podSeparatorGap = if (specialCount > 0) (if (isLarge) 6.dp else 4.dp) else 0.dp
    val podInnerGap = if (specialCount > 1) 4.dp else 0.dp
    val podPadding = if (specialCount > 0) 8.dp else 0.dp

    val regularGapsCount = (regularCount - 1).coerceAtLeast(0)
    val totalGapsAndPodWidth = (regularGap * regularGapsCount) + podSeparatorGap + podPadding + podInnerGap

    val maxFitDiameter = (availableTrayWidth - totalGapsAndPodWidth) / totalBalls
    val targetDiameter = if (isLarge) (if (isWide) 40.dp else 38.dp) else 30.dp
    val minDiameter = if (isLarge) 30.dp else 19.dp
    val ballDiameter = maxFitDiameter.coerceIn(minDiameter, targetDiameter)

    val resolvedSpecialBallLabel = drawState.specialBallLabel?.takeIf { it.isNotBlank() }
        ?: if (drawState.specialNumbers.isNotEmpty()) {
            if (isLotto6aus49) {
                "Superzahl: ${drawState.specialNumbers.first()}"
            } else {
                "${drawState.specialNumbers.size} EUROZAHLEN"
            }
        } else {
            null
        }

    // Outer Border Simulation
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaceBorderColor)
            .cornerRadius(16.dp)
            .padding(1.dp)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(surfaceColor)
                .cornerRadius(15.dp)
        ) {
            // ==================== 1. LEFT GOLDEN JACKPOT ANCHOR STRIP ====================
            Box(
                modifier = GlanceModifier
                    .width(anchorWidth)
                    .fillMaxHeight()
                    .background(anchorBgColor)
                    .padding(vertical = if (isLarge) 10.dp else 6.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(if (isLarge) 32.dp else 24.dp)
                            .background(Color(0x33FFFFFF))
                            .cornerRadius(if (isLotto6aus49) 999.dp else 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLotto6aus49) "🍀" else "E",
                            style = TextStyle(
                                color = onAnchorTitle,
                                fontSize = if (isLarge) {
                                    if (isLotto6aus49) 15.sp else 17.sp
                                } else 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    Text(
                        text = if (isLotto6aus49) "6AUS49" else "EURO",
                        style = TextStyle(
                            color = onAnchorSubtitle,
                            fontSize = if (isLarge) 9.sp else 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = drawState.jackpotAmountValue ?: "–",
                            style = TextStyle(
                                color = onAnchorTitle,
                                fontSize = if (isLarge) 25.sp else 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = drawState.jackpotUnitValue ?: "MIO",
                            style = TextStyle(
                                color = onAnchorSubtitle,
                                fontSize = if (isLarge) 13.sp else 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "EURO",
                            style = TextStyle(
                                color = onAnchorUnit,
                                fontSize = if (isLarge) 8.sp else 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Box(
                        modifier = GlanceModifier
                            .size(if (isLarge) 20.dp else 16.dp)
                            .background(Color(0x26FFFFFF))
                            .cornerRadius(999.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLotto6aus49) "✓" else "★",
                            style = TextStyle(
                                color = onAnchorSubtitle,
                                fontSize = if (isLarge) 10.sp else 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // ==================== 2. MAIN CARD BODY ====================
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .background(surfaceColor),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(
                            start = if (isLarge) 12.dp else 8.dp,
                            top = if (isLarge) 10.dp else 6.dp,
                            end = if (isLarge) 12.dp else 8.dp
                        )
                ) {
                    // Top Header Row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            if (isLotto6aus49) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "LOTTO ",
                                        style = TextStyle(
                                            color = onSurfaceColor,
                                            fontSize = if (isLarge) 18.sp else 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "6aus49",
                                        style = TextStyle(
                                            color = primaryColor,
                                            fontSize = if (isLarge) 14.sp else 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                Text(
                                    text = "Eurojackpot",
                                    style = TextStyle(
                                        color = onSurfaceColor,
                                        fontSize = if (isLarge) 18.sp else 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📅 ",
                                    style = TextStyle(fontSize = if (isLarge) 11.sp else 9.5.sp)
                                )
                                Text(
                                    text = drawState.formattedDate,
                                    style = TextStyle(
                                        color = onSurfaceVariantColor,
                                        fontSize = if (isLarge) 13.sp else 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        if (isLotto6aus49 && drawState.supplementaryGames.isNotEmpty()) {
                            Box(
                                modifier = GlanceModifier
                                    .background(surfaceBorderColor)
                                    .cornerRadius(8.dp)
                                    .padding(1.dp)
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .background(surfaceContainerColor)
                                        .cornerRadius(7.dp)
                                        .padding(
                                            horizontal = if (isLarge) 7.dp else 5.dp,
                                            vertical = if (isLarge) 3.dp else 2.dp
                                        )
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        drawState.supplementaryGames.forEach { game ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${game.name}: ",
                                                    style = TextStyle(
                                                        color = onSurfaceSubtleColor,
                                                        fontSize = if (isLarge) 9.5.sp else 8.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                                Text(
                                                    text = game.winningNumber,
                                                    style = TextStyle(
                                                        color = if (game.name.contains("77", ignoreCase = true)) {
                                                            glanceColor(
                                                                light = Color(0xFFD97706),
                                                                dark = Color(0xFFFBBF24)
                                                            )
                                                        } else {
                                                            glanceColor(
                                                                light = Color(0xFFDC2626),
                                                                dark = Color(0xFFF87171)
                                                            )
                                                        },
                                                        fontSize = if (isLarge) 9.5.sp else 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(if (isLarge) 7.dp else 4.dp))

                    // ==================== SUNKEN BALL TRAY ====================
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = drawState.mainBallsLabel,
                            style = TextStyle(
                                color = onSurfaceSubtleColor,
                                fontSize = if (isLarge) 10.sp else 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        if (resolvedSpecialBallLabel != null) {
                            Text(
                                text = resolvedSpecialBallLabel,
                                style = TextStyle(
                                    color = if (isLotto6aus49) {
                                        glanceColor(light = Color(0xFFDC2626), dark = Color(0xFFF87171))
                                    } else {
                                        glanceColor(light = Color(0xFFD97706), dark = Color(0xFFFBBF24))
                                    },
                                    fontSize = if (isLarge) 10.sp else 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(3.dp))

                    // Sunken Pill Container (Max 8 children inside Row -> Never drops 6th ball or Superzahl!)
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(surfaceBorderColor)
                            .cornerRadius(999.dp)
                            .padding(1.dp)
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(surfaceContainerLowColor)
                                .cornerRadius(999.dp)
                                .padding(horizontal = 6.dp, vertical = if (isLarge) 4.dp else 2.5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Regular Balls: spacing via endPadding so zero extra Spacer children are created
                                drawState.regularNumbers.forEachIndexed { index, number ->
                                    val endSpace = if (index < drawState.regularNumbers.size - 1) regularGap else 0.dp
                                    StaticGlanceBall(
                                        number = number,
                                        backgroundColor = Color(0xFFFFFFFF),
                                        darkBackgroundColor = Color(0xFF1E293B),
                                        borderColor = Color(0xFFE2E8F0),
                                        darkBorderColor = Color(0xFF334155),
                                        textColor = Color(0xFF0F172A),
                                        darkTextColor = Color(0xFFF8FAFC),
                                        diameter = ballDiameter,
                                        endPadding = endSpace
                                    )
                                }

                                // Single flexible Spacer distributes remaining width on wide screens
                                if (drawState.specialNumbers.isNotEmpty()) {
                                    Spacer(modifier = GlanceModifier.defaultWeight())

                                    val (podBg, podBorder) = if (isLotto6aus49) {
                                        glanceColor(light = Color(0xFFFEE2E2), dark = Color(0xFF450A0A)) to
                                                glanceColor(light = Color(0xFFFECACA), dark = Color(0xFF7F1D1D))
                                    } else {
                                        glanceColor(light = Color(0xFFFEF3C7), dark = Color(0xFF451A03)) to
                                                glanceColor(light = Color(0xFFFDE68A), dark = Color(0xFF78350F))
                                    }

                                    val (specialBallLight, specialBallDark) = if (isLotto6aus49) {
                                        Color(0xFFDC2626) to Color(0xFFEF4444)
                                    } else {
                                        Color(0xFFD97706) to Color(0xFFF59E0B)
                                    }

                                    Box(
                                        modifier = GlanceModifier
                                            .background(podBorder)
                                            .cornerRadius(999.dp)
                                            .padding(1.dp)
                                    ) {
                                        Box(
                                            modifier = GlanceModifier
                                                .background(podBg)
                                                .cornerRadius(999.dp)
                                                .padding(horizontal = 3.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                drawState.specialNumbers.forEachIndexed { i, specialNum ->
                                                    val specialEndSpace = if (i < drawState.specialNumbers.size - 1) podInnerGap else 0.dp
                                                    StaticGlanceBall(
                                                        number = specialNum,
                                                        backgroundColor = specialBallLight,
                                                        darkBackgroundColor = specialBallDark,
                                                        borderColor = specialBallLight,
                                                        darkBorderColor = specialBallDark,
                                                        textColor = Color.White,
                                                        darkTextColor = Color.White,
                                                        diameter = ballDiameter,
                                                        endPadding = specialEndSpace
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Flexible Spacer pushes Next Draw Pill flush to the bottom-right corner
                Spacer(modifier = GlanceModifier.defaultWeight())

                // ==================== BOTTOM STATUS (NEXT DRAW PILL) ====================
                val nextDrawText = drawState.nextDrawText
                if (nextDrawText != null) {
                    val prefix = "Nächste Ziehung:"
                    val hasPrefix = nextDrawText.startsWith(prefix, ignoreCase = true)
                    val datePart = if (hasPrefix) nextDrawText.removePrefix(prefix).trim() else nextDrawText

                    val (pillBg, pillLabelColor, pillDateColor) = if (isLotto6aus49) {
                        Triple(
                            glanceColor(Color(0xFFCA8A04)),
                            glanceColor(Color(0xFFFEF3C7)),
                            glanceColor(Color.White)
                        )
                    } else {
                        Triple(
                            glanceColor(Color(0xFF451A03)),
                            glanceColor(Color(0xFFFDE68A)),
                            glanceColor(light = Color(0xFFF59E0B), dark = Color(0xFFFBBF24))
                        )
                    }

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .background(pillBg)
                                .cornerRadius(if (isLarge) 8.dp else 6.dp)
                                .padding(
                                    horizontal = if (isLarge) 12.dp else 8.dp,
                                    vertical = if (isLarge) 4.dp else 2.5.dp
                                )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasPrefix) {
                                    Text(
                                        text = "Nächste Ziehung: ",
                                        style = TextStyle(
                                            color = pillLabelColor,
                                            fontSize = if (isLarge) 11.sp else 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                                Text(
                                    text = datePart,
                                    style = TextStyle(
                                        color = pillDateColor,
                                        fontSize = if (isLarge) 11.sp else 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticGlanceBall(
    number: Int,
    backgroundColor: Color,
    borderColor: Color = backgroundColor,
    textColor: Color,
    diameter: Dp = 29.dp,
    endPadding: Dp = 0.dp,
    darkBackgroundColor: Color = backgroundColor,
    darkBorderColor: Color = borderColor,
    darkTextColor: Color = textColor
) {
    val fontSize = when {
        diameter >= 30.dp -> if (number >= 10) 13.sp else 14.sp
        diameter >= 26.dp -> if (number >= 10) 11.5.sp else 12.5.sp
        diameter >= 22.dp -> if (number >= 10) 10.sp else 11.sp
        else -> if (number >= 10) 8.5.sp else 9.5.sp
    }

    val bgProvider = glanceColor(light = backgroundColor, dark = darkBackgroundColor)
    val borderProvider = glanceColor(light = borderColor, dark = darkBorderColor)
    val textProvider = glanceColor(light = textColor, dark = darkTextColor)
    val hasDistinctBorder = backgroundColor != borderColor || darkBackgroundColor != darkBorderColor

    Box(
        modifier = GlanceModifier.padding(end = endPadding),
        contentAlignment = Alignment.Center
    ) {
        if (hasDistinctBorder) {
            Box(
                modifier = GlanceModifier
                    .size(diameter)
                    .background(borderProvider)
                    .cornerRadius(999.dp)
                    .padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bgProvider)
                        .cornerRadius(999.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        style = TextStyle(
                            color = textProvider,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        } else {
            Box(
                modifier = GlanceModifier
                    .size(diameter)
                    .background(bgProvider)
                    .cornerRadius(999.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = TextStyle(
                        color = textProvider,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// =============================================================================
// PREVIEWS (Android Studio Jetpack Glance Tooling)
// =============================================================================

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 350)
@Composable
fun Lotto24GlanceWidgetLargePreview() {
    GlanceTheme {
        Lotto24DualCardWidgetContent(states = Lotto24GlanceWidget.sampleWidgetStates())
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 420, heightDp = 350)
@Composable
fun Lotto24GlanceWidgetWidePreview() {
    GlanceTheme {
        Lotto24DualCardWidgetContent(states = Lotto24GlanceWidget.sampleWidgetStates())
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 170)
@Composable
fun Lotto24GlanceSingleCard6aus49Preview() {
    GlanceTheme {
        GlanceHomeStyleCard(
            drawState = Lotto24GlanceWidget.sampleWidgetStates().first(),
            modifier = GlanceModifier.fillMaxSize(),
            isLarge = true,
            widgetWidth = 320.dp
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 170)
@Composable
fun Lotto24GlanceSingleCardEurojackpotPreview() {
    GlanceTheme {
        GlanceHomeStyleCard(
            drawState = Lotto24GlanceWidget.sampleWidgetStates().last(),
            modifier = GlanceModifier.fillMaxSize(),
            isLarge = true,
            widgetWidth = 320.dp
        )
    }
}
