package de.lotto24case.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.lotto24case.presentation.model.BallUiModel
import de.lotto24case.presentation.model.BallUiType
import de.lotto24case.presentation.theme.AuraTheme

/**
 * High-fidelity Lottery Ball matching the sunken ball tray specifications from DESIGN.md and code.html:
 * - Regular: semantic ball background, subtle border, onSurface text.
 * - Superzahl: Vibrant rose-red (#E11D48 / #DC2626) background, pure white bold text, rose shadow.
 * - Eurozahlen: Rich gold/amber (#CA8A04) background, pure white bold text, amber shadow.
 * - Colors derived from [AuraTheme.colors].
 */
@Composable
fun LotteryBall(
    model: BallUiModel,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    onClick: () -> Unit = {}
) {
    val colors = AuraTheme.colors

    val (bgBrush, textColor, borderColor, shadowColor) = when (model.type) {
        BallUiType.REGULAR -> {
            Tuple4(
                colors.ballRegularBackground,
                colors.ballRegularText,
                colors.ballRegularBorder,
                Color.Black.copy(alpha = 0.04f)
            )
        }
        BallUiType.SUPER_ZAHL -> {
            Tuple4(
                colors.superzahlRuby,
                Color.White,
                Color.Transparent,
                colors.superzahlRuby.copy(alpha = 0.35f)
            )
        }
        BallUiType.EURO_ZAHL -> {
            Tuple4(
                colors.eurozahlAmber,
                Color.White,
                Color.Transparent,
                colors.eurozahlAmber.copy(alpha = 0.40f)
            )
        }
    }

    val typeLabel = when (model.type) {
        BallUiType.REGULAR -> "Zahl"
        BallUiType.SUPER_ZAHL -> "Superzahl"
        BallUiType.EURO_ZAHL -> "Eurozahl"
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (model.type == BallUiType.REGULAR) 1.dp else 4.dp,
                shape = CircleShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(CircleShape)
            .background(bgBrush)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.dp, borderColor, CircleShape)
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = "$typeLabel ${model.number}"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = model.number.toString(),
            color = textColor,
            fontSize = if (model.number >= 10) 14.sp else 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
