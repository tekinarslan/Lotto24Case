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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.lotto24case.presentation.theme.AuraSpacing
import de.lotto24case.presentation.theme.AuraTheme
import de.lotto24case.presentation.theme.AuraTypography

/**
 * Vertical / Compact Card Badge displaying the jackpot height (e.g. "80 MIO", "6 MIO").
 * Resolves colors directly from [AuraTheme.colors].
 */
@Composable
fun JackpotBadge(
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AuraSpacing.innerBadgeRadius))
            .background(colors.jackpotGold)
            .border(1.dp, colors.jackpotGoldDark, RoundedCornerShape(AuraSpacing.innerBadgeRadius))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = "Jackpot $badgeText" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeText,
            color = colors.onSurface,
            style = AuraTypography.labelSm
        )
    }
}
