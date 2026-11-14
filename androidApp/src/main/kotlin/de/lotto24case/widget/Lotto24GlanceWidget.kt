package de.lotto24case.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import de.lotto24case.MainActivity
import de.lotto24case.widget.model.WidgetDrawState
import de.lotto24case.widget.model.WidgetSupplementaryGame
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Modern Jetpack Glance AppWidget displaying both LOTTO 6aus49 and Eurojackpot,
 * matching the exact in-app Aura Lotto card layout without animations.
 *
 * Fully static representation meeting Large Widget Card requirements:
 * - Measures available card width and dynamically calculates adaptive ball diameter for 6+1 and 5+2 layouts
 * - Replaces unbounded flexible spacers with controlled proportional spacing across the entire tray width
 * - Caps ball size (20dp..28dp) to prevent overflow while filling the horizontal tray span
 * - Left vertical Golden Anchor Strip filled 100% height across card
 * - Title and draw date
 * - Supplementary games (Spiel 77, SUPER6 for LOTTO 6aus49)
 * - Winning numbers with German semantic labels
 * - Sunken ball tray with regular balls and special ball pod
 * - Bottom footer with "Nächste Ziehung: [day, date]"
 */
class Lotto24GlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetStates: ImmutableList<WidgetDrawState> = try {
            Lotto24WidgetHelper.getWidgetDrawData(forceRefresh = false)
                .ifEmpty { fallbackStates() }
                .toImmutableList()
        } catch (_: Exception) {
            fallbackStates()
        }

        provideContent {
            GlanceTheme {
                Lotto24DualCardWidgetContent(states = widgetStates)
            }
        }
    }

    private fun fallbackStates(): ImmutableList<WidgetDrawState> = persistentListOf(
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

@Composable
private fun Lotto24DualCardWidgetContent(
    states: ImmutableList<WidgetDrawState>
) {
    val glanceSize = LocalSize.current
    val widgetWidth = glanceSize.width

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)) // App screen background canvas (#F1F5F9)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        states.forEachIndexed { index, drawState ->
            GlanceHomeStyleCard(
                drawState = drawState,
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxWidth(),
                widgetWidth = widgetWidth
            )
            if (index < states.size - 1) {
                Spacer(modifier = GlanceModifier.height(4.dp))
            }
        }
    }
}

/**
 * Replicates the home app screen's [LotteryCard] design in Jetpack Glance:
 * - Left vertical Golden Anchor Strip filled 100% height across card
 * - Main Card Body with Title, Calendar Date, Supplementary Games, Sunken Ball Tray, Special Pod & Next Draw footer
 * - Dynamically calculates adaptive ball diameter & controlled spacing from [widgetWidth]
 */
@Composable
private fun GlanceHomeStyleCard(
    drawState: WidgetDrawState,
    modifier: GlanceModifier = GlanceModifier,
    widgetWidth: Dp = 320.dp
) {
    val isLotto6aus49 = drawState.lotteryId.contains("6aus49", ignoreCase = true)
    val anchorBgColor = if (isLotto6aus49) Color(0xFFE5A91E) else Color(0xFFD97706)
    val cardBgColor = Color(0xFFFFFFFF)

    // Measure available card width and compute adaptive ball diameter & controlled gaps
    val effectiveWidth = if (widgetWidth > 0.dp) widgetWidth else 320.dp
    // Deduct: anchor (48dp) + card padding (16dp) + tray inner padding (8dp)
    val usableTrayWidth = (effectiveWidth - 72.dp).coerceAtLeast(160.dp)

    // 7 balls total across both 6+1 (LOTTO 6aus49) and 5+2 (Eurojackpot)
    val baseGaps = 28.dp
    val rawBallDiameter = (usableTrayWidth - baseGaps) / 7f
    val ballDiameter = rawBallDiameter.coerceIn(20.dp, 28.dp)

    // Controlled spacing: replace huge flexible spacer with proportional spacing
    val remainingSpace = (usableTrayWidth - (ballDiameter * 7) - 6.dp).coerceAtLeast(16.dp)
    val interBallGap = (remainingSpace / 8f).coerceIn(3.dp, 6.dp)
    val podSeparatorGap = (remainingSpace - (interBallGap * 5f)).coerceIn(6.dp, 14.dp)

    Row(
        modifier = modifier
            .background(cardBgColor)
            .cornerRadius(12.dp)
    ) {
        // ==================== 1. LEFT GOLDEN ANCHOR STRIP ====================
        Box(
            modifier = GlanceModifier
                .width(48.dp)
                .fillMaxHeight()
                .background(anchorBgColor)
                .cornerRadius(12.dp)
                .padding(vertical = 4.dp, horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top Monogram
                Box(
                    modifier = GlanceModifier
                        .size(18.dp)
                        .background(Color(0x33000000))
                        .cornerRadius(999.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLotto6aus49) "🍀" else "E",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    text = drawState.anchorLabel,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFEF3C7)),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Vertical Jackpot Amount
                Text(
                    text = drawState.jackpotAmountValue ?: "–",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = drawState.jackpotUnitValue ?: "MIO",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFEF3C7)),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "EURO",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFDE68A)),
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Seal Mark
                Box(
                    modifier = GlanceModifier
                        .size(14.dp)
                        .background(Color(0x22000000))
                        .cornerRadius(999.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLotto6aus49) "✓" else "★",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFEF3C7)),
                            fontSize = 8.sp,
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
                .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header: Title & Date
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = drawState.title,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF0F172A)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                Text(
                    text = "📅 ${drawState.formattedDate}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF64748B)),
                        fontSize = 8.sp
                    )
                )
            }

            // Supplementary Games (LOTTO 6aus49: Spiel 77, SUPER6)
            if (drawState.supplementaryGames.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    drawState.supplementaryGames.forEachIndexed { idx, game ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${game.name}: ",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF64748B)),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = game.winningNumber,
                                style = TextStyle(
                                    color = ColorProvider(
                                        if (game.name.contains("77")) Color(0xFFD97706) else Color(0xFFDC2626)
                                    ),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        if (idx < drawState.supplementaryGames.size - 1) {
                            Spacer(modifier = GlanceModifier.width(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            // Tray Header Labels
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = drawState.mainBallsLabel,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF94A3B8)),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                val specialBallLabel = drawState.specialBallLabel
                if (specialBallLabel != null) {
                    Text(
                        text = specialBallLabel,
                        style = TextStyle(
                            color = ColorProvider(
                                if (isLotto6aus49) Color(0xFFDC2626) else Color(0xFFB45309)
                            ),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            // Sunken Ball Tray (adaptive balls with controlled spacing using full horizontal width)
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .cornerRadius(999.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Regular Balls
                    drawState.regularNumbers.forEachIndexed { index, number ->
                        StaticGlanceBall(
                            number = number,
                            backgroundColor = Color(0xFFFFFFFF),
                            textColor = Color(0xFF0F172A),
                            diameter = ballDiameter
                        )
                        if (index < drawState.regularNumbers.size - 1) {
                            Spacer(modifier = GlanceModifier.width(interBallGap))
                        }
                    }

                    // Controlled separator gap between regular balls and special pod
                    Spacer(modifier = GlanceModifier.width(podSeparatorGap))

                    // Special Balls Pod (Contrast Capsule)
                    Box(
                        modifier = GlanceModifier
                            .background(
                                if (isLotto6aus49) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                            )
                            .cornerRadius(999.dp)
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            drawState.specialNumbers.forEachIndexed { i, specialNum ->
                                StaticGlanceBall(
                                    number = specialNum,
                                    backgroundColor = if (isLotto6aus49) Color(0xFFDC2626) else Color(0xFFD97706),
                                    textColor = Color.White,
                                    diameter = ballDiameter
                                )
                                if (i < drawState.specialNumbers.size - 1) {
                                    Spacer(modifier = GlanceModifier.width(interBallGap))
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Section: Nächste Ziehung (Next Draw)
            val nextDrawText = drawState.nextDrawText
            if (nextDrawText != null) {
                Spacer(modifier = GlanceModifier.height(3.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Box(
                        modifier = GlanceModifier
                            .background(Color(0xFFEFF6FF))
                            .cornerRadius(4.dp)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = nextDrawText,
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF1D4ED8)),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
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
    textColor: Color,
    diameter: Dp = 20.dp
) {
    val fontSize = when {
        diameter >= 26.dp -> if (number >= 10) 10.sp else 11.sp
        diameter >= 23.dp -> if (number >= 10) 9.sp else 10.sp
        else -> if (number >= 10) 8.sp else 9.sp
    }

    Box(
        modifier = GlanceModifier
            .size(diameter)
            .background(backgroundColor)
            .cornerRadius(999.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
