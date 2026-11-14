package de.lotto24case.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
 * - Left vertical Golden Anchor Strip with Monogram, Anchor Label, Jackpot & Bottom Seal
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
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)) // App screen background canvas (#F1F5F9)
            .cornerRadius(16.dp)
            .padding(6.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        states.forEachIndexed { index, drawState ->
            GlanceHomeStyleCard(drawState = drawState)
            if (index < states.size - 1) {
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }
    }
}

/**
 * Replicates the home app screen's [LotteryCard] design in Jetpack Glance:
 * - Left vertical Golden Anchor Strip with Monogram, Anchor Label, Jackpot & Bottom Seal
 * - Main Card Body with Title, Calendar Date, Supplementary Games, Sunken Ball Tray, Special Pod & Next Draw footer
 */
@Composable
private fun GlanceHomeStyleCard(
    drawState: WidgetDrawState
) {
    val isLotto6aus49 = drawState.lotteryId.contains("6aus49", ignoreCase = true)
    val anchorBgColor = if (isLotto6aus49) Color(0xFFE5A91E) else Color(0xFFD97706)
    val cardBgColor = Color(0xFFFFFFFF)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(cardBgColor)
            .cornerRadius(12.dp)
    ) {
        // ==================== 1. LEFT GOLDEN ANCHOR STRIP ====================
        Box(
            modifier = GlanceModifier
                .width(48.dp)
                .background(anchorBgColor)
                .padding(vertical = 6.dp, horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Monogram
                Box(
                    modifier = GlanceModifier
                        .size(20.dp)
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

                Spacer(modifier = GlanceModifier.height(2.dp))

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

                Spacer(modifier = GlanceModifier.height(2.dp))

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
                .padding(start = 8.dp, top = 6.dp, end = 8.dp, bottom = 6.dp)
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

            // Sunken Ball Tray
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
                    drawState.regularNumbers.forEach { number ->
                        StaticGlanceBall(
                            number = number,
                            backgroundColor = Color(0xFFFFFFFF),
                            textColor = Color(0xFF0F172A),
                            size = 19
                        )
                        Spacer(modifier = GlanceModifier.width(2.dp))
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Special Balls Pod (Contrast Capsule)
                    Box(
                        modifier = GlanceModifier
                            .background(
                                if (isLotto6aus49) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                            )
                            .cornerRadius(999.dp)
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            drawState.specialNumbers.forEachIndexed { i, specialNum ->
                                StaticGlanceBall(
                                    number = specialNum,
                                    backgroundColor = if (isLotto6aus49) Color(0xFFDC2626) else Color(0xFFD97706),
                                    textColor = Color.White,
                                    size = 19
                                )
                                if (i < drawState.specialNumbers.size - 1) {
                                    Spacer(modifier = GlanceModifier.width(2.dp))
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
    size: Int = 19
) {
    Box(
        modifier = GlanceModifier
            .size(size.dp)
            .background(backgroundColor)
            .cornerRadius(999.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = if (number >= 10) 8.sp else 9.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
