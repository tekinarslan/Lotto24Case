package de.lotto24case.presentation.mapper

import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.domain.model.Money
import de.lotto24case.presentation.formatter.CurrencyFormatter
import de.lotto24case.presentation.formatter.LotteryDateTimeFormatter
import de.lotto24case.presentation.model.BallUiModel
import de.lotto24case.presentation.model.BallUiType
import de.lotto24case.presentation.model.LotteryCardUiModel
import de.lotto24case.presentation.model.SupplementaryGameUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/**
 * Maps pure Kotlin domain models ([LotteryDraw]) into stable Compose UI models ([LotteryCardUiModel]).
 */
object LotteryUiMapper {

    private const val CENTS_PER_MILLION = 100_000_000L

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

        // Resolve API timezone, with fallback per lottery rules
        val fallbackZone = when (draw.lotteryType) {
            LotteryType.LOTTO_6AUS49 -> LotteryDateTimeFormatter.TIMEZONE_BERLIN
            LotteryType.EUROJACKPOT -> LotteryDateTimeFormatter.TIMEZONE_HELSINKI
        }
        val timeZone = if (!draw.timeZone.isNullOrBlank()) {
            LotteryDateTimeFormatter.resolveTimeZone(draw.timeZone)
        } else {
            fallbackZone
        }

        val formattedDate = LotteryDateTimeFormatter.formatDrawDate(draw.drawDate, timeZone) ?: "–"

        // Next draw date badge text
        val nextDrawBadgeText = LotteryDateTimeFormatter.formatNextDraw(draw.nextDrawDate, timeZone)

        // Deterministic remaining interval between drawDate and nextDrawDate
        val remainingTimeText = LotteryDateTimeFormatter.formatRemainingTime(
            nextDrawDate = draw.nextDrawDate,
            drawDate = draw.drawDate
        )

        val jackpotMoney = draw.jackpot?.amount
        val formattedJackpot = CurrencyFormatter.formatJackpot(jackpotMoney)
        val (jackpotAmountVal, jackpotUnitVal) = formatAnchorJackpot(jackpotMoney)

        var superNumberValue: Int? = null
        val mainBallsLabel: String
        val specialBallLabel: String?

        val (balls, supplementaryGames) = when (draw) {
            is LotteryDraw.Lotto6aus49 -> {
                superNumberValue = draw.superNumber
                mainBallsLabel = "GEWINNZAHLEN"
                specialBallLabel = draw.superNumber?.let { "Superzahl: $it" }

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
                mainBallsLabel = "5 GEWINNZAHLEN"
                specialBallLabel = "2 EUROZAHLEN"

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
            nextDrawBadgeText = nextDrawBadgeText,
            remainingTimeText = remainingTimeText,
            jackpotFormatted = formattedJackpot,
            jackpotAmountValue = jackpotAmountVal,
            jackpotUnitValue = jackpotUnitVal,
            mainBallsLabel = mainBallsLabel,
            specialBallLabel = specialBallLabel,
            balls = balls,
            superNumber = superNumberValue,
            supplementaryGames = supplementaryGames,
            hasJackpot = draw.jackpot?.hasAmount == true
        )
    }

    private fun formatAnchorJackpot(money: Money?): Pair<String?, String?> {
        if (money == null || money.minorUnits <= 0L) return null to null
        return if (money.minorUnits >= CENTS_PER_MILLION) {
            val millions = (money.minorUnits / CENTS_PER_MILLION).toString()
            millions to "MIO"
        } else {
            val thousands = (money.minorUnits / 100_000L).toString()
            thousands to "Tsd"
        }
    }
}
