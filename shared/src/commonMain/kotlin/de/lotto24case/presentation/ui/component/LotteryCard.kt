package de.lotto24case.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.presentation.model.BallUiType
import de.lotto24case.presentation.model.LotteryCardUiModel

/**
 * Clean & Production-grade Aura Lotto Card:
 * - Left vertical Golden / Dark-Amber Jackpot Anchor Strip with "EURO" and "MIO".
 * - Header with lottery brand title, calendar icon, and draw date.
 * - Supplementary game pill mini-box (Spiel 77, SUPER6) for LOTTO 6aus49 directly parsed from response.
 * - Sunken Ball Tray (`bg-slate-100/80` pill shape) with German labels and special ball inner contrast pod.
 * - Bottom status bar with remaining time pill (e.g. "⏱ 3 Tage 23 Std.") and NextDrawPill ("Nächste Ziehung: ...").
 */
@Composable
fun LotteryCard(
    model: LotteryCardUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isDark: Boolean = isSystemInDarkTheme()
) {
    val cardBg = if (isDark) Color(0xFF131B2E) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val isLotto6aus49 = model.lotteryType == LotteryType.LOTTO_6AUS49

    val anchorGradient = if (isLotto6aus49) {
        listOf(Color(0xFFEAB308), Color(0xFFCA8A04), Color(0xFFA16207))
    } else {
        listOf(Color(0xFFCA8A04), Color(0xFFB45309), Color(0xFF78350F))
    }

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
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // ==================== LEFT GOLDEN JACKPOT ANCHOR STRIP ====================
            Box(
                modifier = Modifier
                    .width(84.dp)
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
                                .background(Color.White.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLotto6aus49) "🍀" else "E",
                                color = Color.White,
                                fontSize = if (isLotto6aus49) 16.sp else 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = if (isLotto6aus49) "6AUS49" else "EURO",
                            color = Color(0xFFFEF3C7),
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
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = model.jackpotUnitValue ?: "MIO",
                            color = Color(0xFFFEF3C7),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "EURO",
                            color = Color(0xFFFDE68A),
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
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLotto6aus49) "✓" else "★",
                            color = Color(0xFFFEF3C7),
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
                                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "6aus49",
                                        color = Color(0xFFCA8A04),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Eurojackpot",
                                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
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
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155),
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
                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                    .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                model.supplementaryGames.forEach { game ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${game.name}:",
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = game.winningNumber,
                                            color = if (game.name.contains("77", ignoreCase = true)) Color(0xFFCA8A04) else Color(0xFFE11D48),
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
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            if (model.specialBallLabel != null) {
                                Text(
                                    text = model.specialBallLabel,
                                    color = if (isLotto6aus49) Color(0xFFE11D48) else Color(0xFFB45309),
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
                                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
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
                                        size = if (isLotto6aus49) 32.dp else 31.dp,
                                        isDark = isDark
                                    )
                                }

                                // Special Balls Pod (Inner Contrast Capsule)
                                if (specialBalls.isNotEmpty()) {
                                    val specialPodBg = if (isDark) {
                                        Color(0xFF1E293B)
                                    } else {
                                        if (isLotto6aus49) Color(0xFFFDE8E8) else Color(0xFFFEF3C7)
                                    }
                                    val specialPodBorder = if (isDark) {
                                        Color(0xFF334155)
                                    } else {
                                        if (isLotto6aus49) Color(0xFFFCA5A5) else Color(0xFFFDE68A)
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
                                                    size = if (isLotto6aus49) 32.dp else 31.dp,
                                                    isDark = isDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==================== BOTTOM STATUS (REMAINING TIME & NEXT DRAW PILL) ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Remaining Time Pill (e.g. "⏱ 3 Tage 23 Std.")
                    if (model.remainingTimeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                )
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFFEF3C7))
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color(0xFF334155) else Color(0xFFFDE68A),
                                    shape = RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("⏱", fontSize = 10.sp)
                                Text(
                                    text = model.remainingTimeText,
                                    color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Next Draw Pill (e.g. "Nächste Ziehung: Mi., 23.09.2020")
                    if (model.nextDrawBadgeText != null) {
                        NextDrawPill(
                            text = model.nextDrawBadgeText,
                            lotteryType = model.lotteryType,
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}
