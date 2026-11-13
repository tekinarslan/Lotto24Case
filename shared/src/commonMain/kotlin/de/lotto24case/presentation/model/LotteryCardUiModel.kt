package de.lotto24case.presentation.model

import androidx.compose.runtime.Immutable
import de.lotto24case.domain.model.LotteryType
import kotlinx.collections.immutable.ImmutableList

/**
 * Immutable UI model representing a full lottery draw card.
 *
 * @property id Stable key for LazyColumn items (e.g. "6aus49", "eurojackpot").
 * @property lotteryType Main lottery type.
 * @property drawIdentifier Unique draw date/identifier (e.g. "2023-01-21") for animation key stabilization.
 * @property title Header title (e.g. "LOTTO 6aus49", "Eurojackpot").
 * @property formattedDrawDate Formatted date string (e.g. "Fr., 18.09.2020").
 * @property nextDrawBadgeText Formatted next draw badge string (e.g. "Nächste Ziehung: Mi., 23.09.2020"), or null if unavailable.
 * @property remainingTimeText Remaining duration until next draw (e.g. "3 Tage 23 Std."), or null if unavailable.
 * @property jackpotFormatted Full formatted jackpot string (e.g. "80 MIO €"), or null if not yet determined.
 * @property jackpotAmountValue Numerical jackpot string for left golden anchor (e.g. "23", "80"), or null.
 * @property jackpotUnitValue Unit string for left golden anchor (e.g. "MIO"), or null.
 * @property mainBallsLabel German section title above balls (e.g. "GEWINNZAHLEN", "5 GEWINNZAHLEN").
 * @property specialBallLabel German special number label (e.g. "Superzahl: 7", "2 EUROZAHLEN"), or null.
 * @property balls Ordered list of balls to render (regular + special numbers).
 * @property superNumber Special single super number if present.
 * @property supplementaryGames List of supplementary lotteries (Spiel 77, SUPER6), or empty.
 * @property hasJackpot Whether this draw has an active jackpot.
 */
@Immutable
data class LotteryCardUiModel(
    val id: String,
    val lotteryType: LotteryType,
    val drawIdentifier: String = "",
    val title: String,
    val formattedDrawDate: String,
    val nextDrawBadgeText: String?,
    val remainingTimeText: String?,
    val jackpotFormatted: String?,
    val jackpotAmountValue: String?,
    val jackpotUnitValue: String?,
    val mainBallsLabel: String,
    val specialBallLabel: String?,
    val balls: ImmutableList<BallUiModel>,
    val regularBalls: ImmutableList<BallUiModel> = kotlinx.collections.immutable.persistentListOf(),
    val specialBalls: ImmutableList<BallUiModel> = kotlinx.collections.immutable.persistentListOf(),
    val superNumber: Int? = null,
    val supplementaryGames: ImmutableList<SupplementaryGameUiModel>,
    val hasJackpot: Boolean
)
