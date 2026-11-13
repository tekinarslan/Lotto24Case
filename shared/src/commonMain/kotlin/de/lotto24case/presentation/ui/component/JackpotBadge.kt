package de.lotto24case.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.lotto24case.presentation.theme.AuraColors
import de.lotto24case.presentation.theme.AuraSpacing
import de.lotto24case.presentation.theme.AuraTypography

/**
 * Vertical / Compact Card Badge displaying the jackpot height (e.g. "80 MIO", "6 MIO").
 */
@Composable
fun JackpotBadge(
    badgeText: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AuraColors.JackpotGold,
    textColor: Color = AuraColors.OnSurface
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AuraSpacing.innerBadgeRadius))
            .background(backgroundColor)
            .border(1.dp, AuraColors.JackpotGoldDark, RoundedCornerShape(AuraSpacing.innerBadgeRadius))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = "Jackpot $badgeText" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeText,
            color = textColor,
            style = AuraTypography.labelSm
        )
    }
}
