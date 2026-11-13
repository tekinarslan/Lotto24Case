package de.lotto24case.presentation.mapper

import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.domain.util.CurrencyFormatter
import de.lotto24case.domain.util.LotteryDateTimeFormatter
import de.lotto24case.presentation.model.BallUiModel
import de.lotto24case.presentation.model.BallUiType
import de.lotto24case.presentation.model.LotteryCardUiModel
import de.lotto24case.presentation.model.SupplementaryGameUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/**
 * Maps pure Kotlin domain models ([LotteryDraw]) into stable Compose UI models ([LotteryCardUiModel]).
 *
 * Clean Architecture enforcement:
 * - Domain layer is completely decoupled from presentation models.
 * - [BallUiType] and [SupplementaryGameUiModel] are UI-owned types.
 * - Timezone from domain ([LotteryDraw.timeZone]) is explicitly resolved and passed to formatters,
 *   ensuring Eurojackpot (Europe/Helsinki) and 6aus49 (Europe/Berlin) are rendered in their native draw zones.
 * - Single Source of Truth: ball list is derived directly here rather than duplicated in domain.
 */
object LotteryUiMapper {

    /**
     * Maps a list of domain draws into a list of UI card models.
     */
    fun mapDraws(draws: List<LotteryDraw>): List<LotteryCardUiModel> {
        return draws.map { mapSingle(it) }
    }

    /**
     * Maps a single [LotteryDraw] to [LotteryCardUiModel].
     */
    fun mapSingle(draw: LotteryDraw): LotteryCardUiModel {
        val title = when (draw.lotteryType) {
            LotteryType.LOTTO_6AUS49 -> "LOTTO 6aus49"
            LotteryType.EUROJACKPOT -> "Eurojackpot"
        }

        // Explicitly respect lottery-specific timezone (e.g. Helsinki for Eurojackpot, Berlin for 6aus49)
        val timeZone = LotteryDateTimeFormatter.resolveTimeZone(draw.timeZone)
        val formattedDate = LotteryDateTimeFormatter.formatDrawDate(draw.drawDate, timeZone) ?: "–"
        val nextDrawBadge = LotteryDateTimeFormatter.formatNextDraw(draw.nextDrawDate, timeZone)
        val jackpotAmount = draw.jackpot?.amount
        val formattedJackpot = CurrencyFormatter.formatJackpot(jackpotAmount)
        val jackpotBadge = CurrencyFormatter.formatBadgeMio(jackpotAmount)

        val (balls, supplementaryGames) = when (draw) {
            is LotteryDraw.Lotto6aus49 -> {
                val ballList = buildList {
                    draw.numbers.forEach { number ->
                        add(BallUiModel(number = number, type = BallUiType.REGULAR))
                    }
                    draw.superNumber?.let { superNum ->
                        add(BallUiModel(number = superNum, type = BallUiType.SUPER_ZAHL))
                    }
                }.toPersistentList()

                val games = draw.supplementaryLotteries.map { domainGame ->
                    SupplementaryGameUiModel(
                        name = domainGame.name,
                        winningNumber = domainGame.winningNumber
                    )
                }.toPersistentList()

                ballList to games
            }
            is LotteryDraw.Eurojackpot -> {
                val ballList = buildList {
                    draw.numbers.forEach { number ->
                        add(BallUiModel(number = number, type = BallUiType.REGULAR))
                    }
                    draw.euroNumbers.forEach { euroNum ->
                        add(BallUiModel(number = euroNum, type = BallUiType.EURO_ZAHL))
                    }
                }.toPersistentList()

                ballList to persistentListOf<SupplementaryGameUiModel>()
            }
        }

        return LotteryCardUiModel(
            id = draw.lotteryType.id,
            lotteryType = draw.lotteryType,
            title = title,
            formattedDrawDate = formattedDate,
            nextDrawBadgeText = nextDrawBadge,
            jackpotFormatted = formattedJackpot,
            jackpotBadgeFormatted = jackpotBadge,
            balls = balls,
            supplementaryGames = supplementaryGames,
            hasJackpot = draw.jackpot?.hasAmount == true
        )
    }
}
