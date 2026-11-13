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

        val (balls, regularBalls, specialBalls, supplementaryGames) = when (draw) {
            is LotteryDraw.Lotto6aus49 -> {
                superNumberValue = draw.superNumber
                mainBallsLabel = "GEWINNZAHLEN"
                specialBallLabel = draw.superNumber?.let { "Superzahl: $it" }

                val regList = draw.numbers.mapIndexed { index, number ->
                    BallUiModel(number = number, type = BallUiType.REGULAR, index = index)
                }.toPersistentList()

                val specList = if (draw.superNumber != null) {
                    persistentListOf(BallUiModel(number = draw.superNumber, type = BallUiType.SUPER_ZAHL, index = draw.numbers.size))
                } else persistentListOf()

                val ballList = (regList + specList).toPersistentList()

                val games = draw.supplementaryLotteries.map { domainGame ->
                    SupplementaryGameUiModel(
                        name = domainGame.name,
                        winningNumber = domainGame.winningNumber
                    )
                }.toPersistentList()

                Tuple4(ballList, regList, specList, games)
            }
            is LotteryDraw.Eurojackpot -> {
                mainBallsLabel = "5 GEWINNZAHLEN"
                specialBallLabel = "2 EUROZAHLEN"

                val regList = draw.numbers.mapIndexed { index, number ->
                    BallUiModel(number = number, type = BallUiType.REGULAR, index = index)
                }.toPersistentList()

                val specList = draw.euroNumbers.mapIndexed { euroIndex, euroNum ->
                    BallUiModel(number = euroNum, type = BallUiType.EURO_ZAHL, index = draw.numbers.size + euroIndex)
                }.toPersistentList()

                val ballList = (regList + specList).toPersistentList()

                Tuple4(ballList, regList, specList, persistentListOf())
            }
        }

        return LotteryCardUiModel(
            id = draw.lotteryType.id,
            lotteryType = draw.lotteryType,
            drawIdentifier = draw.drawIdentifier,
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
            regularBalls = regularBalls,
            specialBalls = specialBalls,
            superNumber = superNumberValue,
            supplementaryGames = supplementaryGames,
            hasJackpot = draw.jackpot?.hasAmount == true
        )
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

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
