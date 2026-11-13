package de.lotto24case.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.presentation.model.BallUiType
import de.lotto24case.presentation.model.LotteryCardUiModel
import de.lotto24case.presentation.theme.AuraTheme

/**
 * Clean & Production-grade Aura Lotto Card:
 * - Left vertical Golden / Dark-Amber Jackpot Anchor Strip with "EURO" and "MIO".
 * - Header with lottery brand title, calendar icon, and draw date.
 * - Supplementary game pill mini-box (Spiel 77, SUPER6) for LOTTO 6aus49 directly parsed from response.
 * - Sunken Ball Tray (`bg-slate-100/80` pill shape) with German labels and special ball inner contrast pod.
 * - Bottom status bar with remaining time pill and NextDrawPill.
 * - 100% of colors resolved semantically via [AuraTheme.colors]. Zero hardcoded Color(...) values.
 */
@Composable
fun LotteryCard(
    model: LotteryCardUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val colors = AuraTheme.colors
    val cardBg = colors.surface
    val cardBorder = colors.surfaceBorder

    val isLotto6aus49 = model.lotteryType == LotteryType.LOTTO_6AUS49
    val anchorGradient = if (isLotto6aus49) colors.anchorLottoGradient else colors.anchorEuroGradient

    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(cardShape)
            .background(cardBg)
            .border(1.dp, cardBorder, cardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = "${model.title} Ziehung vom ${model.formattedDrawDate}"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // ==================== LEFT GOLDEN JACKPOT ANCHOR STRIP ====================
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(Brush.verticalGradient(anchorGradient))
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Logo / Monogram
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(if (isLotto6aus49) CircleShape else RoundedCornerShape(8.dp))
                                .background(colors.onAnchorTitle.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLotto6aus49) "🍀" else "E",
                                color = colors.onAnchorTitle,
                                fontSize = if (isLotto6aus49) 16.sp else 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = if (isLotto6aus49) "6AUS49" else "EURO",
                            color = colors.onAnchorSubtitle,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Vertical Jackpot Amount
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = model.jackpotAmountValue ?: "–",
                            color = colors.onAnchorTitle,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = model.jackpotUnitValue ?: "MIO",
                            color = colors.onAnchorSubtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "EURO",
                            color = colors.onAnchorUnit,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Bottom Seal Mark
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(colors.onAnchorTitle.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLotto6aus49) "✓" else "★",
                            color = colors.onAnchorSubtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ==================== MAIN CARD BODY ====================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(cardBg),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, top = 14.dp, end = 14.dp)
                ) {
                    // Top Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            // Title
                            if (isLotto6aus49) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "LOTTO ",
                                        color = colors.onSurface,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "6aus49",
                                        color = colors.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Eurojackpot",
                                    color = colors.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Formatted Date with Calendar Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "📅 ",
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = model.formattedDrawDate,
                                    color = colors.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Top-right side box for LOTTO 6aus49 supplementary games
                        if (isLotto6aus49 && model.supplementaryGames.isNotEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceContainer)
                                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                model.supplementaryGames.forEach { game ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${game.name}:",
                                            color = colors.onSurfaceSubtle,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = game.winningNumber,
                                            color = if (game.name.contains("77", ignoreCase = true)) colors.supplementary77Number else colors.supplementarySuper6Number,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ==================== SUNKEN BALL TRAY ====================
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = model.mainBallsLabel,
                                color = colors.onSurfaceSubtle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            if (model.specialBallLabel != null) {
                                Text(
                                    text = model.specialBallLabel,
                                    color = if (isLotto6aus49) colors.superzahlRuby else colors.eurozahlAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = if (isLotto6aus49) 0.sp else 0.5.sp
                                )
                            }
                        }

                        // Sunken Pill Container with Inner Pod
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.surfaceContainerLow)
                                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(999.dp))
                                .padding(horizontal = 6.dp, vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val regularBalls = model.balls.filter { it.type == BallUiType.REGULAR }
                                val specialBalls = model.balls.filter { it.type != BallUiType.REGULAR }

                                // Regular Balls
                                regularBalls.forEach { ball ->
                                    LotteryBall(
                                        model = ball,
                                        size = if (isLotto6aus49) 32.dp else 31.dp
                                    )
                                }

                                // Special Balls Pod (Inner Contrast Capsule)
                                if (specialBalls.isNotEmpty()) {
                                    val (specialPodBg, specialPodBorder) = if (isLotto6aus49) {
                                        colors.superzahlPodBackground to colors.superzahlPodBorder
                                    } else {
                                        colors.eurozahlPodBackground to colors.eurozahlPodBorder
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(specialPodBg)
                                            .border(1.dp, specialPodBorder, RoundedCornerShape(999.dp))
                                            .padding(horizontal = 3.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            specialBalls.forEach { ball ->
                                                LotteryBall(
                                                    model = ball,
                                                    size = if (isLotto6aus49) 32.dp else 31.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Remaining Time Pill positioned between drawn numbers and next draw pill (aligned to the right)
                    if (model.remainingTimeText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.remainingTimeBackground)
                                    .border(
                                        width = 1.dp,
                                        color = colors.remainingTimeBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("⏱", fontSize = 10.sp)
                                    Text(
                                        text = model.remainingTimeText,
                                        color = colors.remainingTimeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==================== BOTTOM STATUS (NEXT DRAW PILL) ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Next Draw Pill (e.g. "Nächste Ziehung: Mi., 23.09.2020")
                    if (model.nextDrawBadgeText != null) {
                        NextDrawPill(
                            text = model.nextDrawBadgeText,
                            lotteryType = model.lotteryType
                        )
                    }
                }
            }
        }
    }
}
