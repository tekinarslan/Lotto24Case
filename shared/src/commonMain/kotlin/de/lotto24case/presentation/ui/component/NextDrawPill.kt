package de.lotto24case.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.presentation.theme.AuraTheme

/**
 * Next Draw badge pill indicator positioned at the bottom-right of the card.
 *
 * Implements the Aura Lotto design specification using semantic [AuraTheme.colors]:
 * - LOTTO 6aus49: Rich Amber/Gold background, top-left and bottom-right rounded corner.
 * - Eurojackpot: Deep warm espresso/dark amber background.
 * - Text content aligned to the right (end), displaying:
 *   "Nächste Ziehung:" in subtle warm tint + date in bold text.
 */
@Composable
fun NextDrawPill(
    text: String,
    modifier: Modifier = Modifier,
    lotteryType: LotteryType = LotteryType.LOTTO_6AUS49
) {
    val is6aus49 = lotteryType == LotteryType.LOTTO_6AUS49
    val colors = AuraTheme.colors

    val (bg, borderColor, labelColor, dateColor) = if (is6aus49) {
        listOf(
            colors.nextDraw6aus49Background,
            colors.nextDraw6aus49Border,
            colors.nextDraw6aus49Label,
            colors.nextDraw6aus49Date
        )
    } else {
        listOf(
            colors.nextDrawEuroBackground,
            colors.nextDrawEuroBorder,
            colors.nextDrawEuroLabel,
            colors.nextDrawEuroDate
        )
    }

    val shape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 0.dp,
        bottomEnd = 12.dp,
        bottomStart = 0.dp
    )

    // Separate "Nächste Ziehung:" prefix if present
    val prefix = "Nächste Ziehung:"
    val hasPrefix = text.startsWith(prefix, ignoreCase = true)
    val datePart = if (hasPrefix) text.removePrefix(prefix).trim() else text

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .semantics { contentDescription = text },
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (hasPrefix) {
                Text(
                    text = "Nächste Ziehung: ",
                    color = labelColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = datePart,
                color = dateColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
