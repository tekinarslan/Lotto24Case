package de.lotto24case.presentation.model

import androidx.compose.runtime.Immutable
import de.lotto24case.domain.model.LotteryType
import kotlinx.collections.immutable.ImmutableList

/**
 * Immutable UI model representing a full lottery draw card.
 *
 * Adheres to Compose stability best practices:
 * - Marked with [@Immutable].
 * - Uses [ImmutableList] for ball and supplementary collections.
 * - Formatted strings ready for direct presentation without UI computation.
 * - Presentation layer is decoupled from domain entities.
 *
 * @property id Stable key for LazyColumn items (e.g. "6aus49", "eurojackpot").
 * @property lotteryType Main lottery type.
 * @property title Header title (e.g. "LOTTO 6aus49", "Eurojackpot").
 * @property formattedDrawDate Formatted date string (e.g. "Fr., 18.09.2020").
 * @property nextDrawBadgeText Formatted next draw pill text (e.g. "Nächste Ziehung: Mi., 23.09.2020"), or null if unavailable.
 * @property jackpotFormatted Full formatted jackpot string (e.g. "80 MIO €"), or null if not yet determined.
 * @property jackpotBadgeFormatted Vertical card banner string (e.g. "80 MIO", "23 MIO"), or null if below threshold.
 * @property balls Ordered list of balls to render (regular + special numbers).
 * @property supplementaryGames List of supplementary lotteries (Spiel 77, SUPER6), or empty.
 * @property hasJackpot Whether this draw has an active jackpot.
 */
@Immutable
data class LotteryCardUiModel(
    val id: String,
    val lotteryType: LotteryType,
    val title: String,
    val formattedDrawDate: String,
    val nextDrawBadgeText: String?,
    val jackpotFormatted: String?,
    val jackpotBadgeFormatted: String?,
    val balls: ImmutableList<BallUiModel>,
    val supplementaryGames: ImmutableList<SupplementaryGameUiModel>,
    val hasJackpot: Boolean
)
