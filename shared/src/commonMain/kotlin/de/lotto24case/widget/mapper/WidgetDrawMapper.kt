package de.lotto24case.widget.mapper

import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.domain.model.Money
import de.lotto24case.presentation.formatter.CurrencyFormatter
import de.lotto24case.presentation.formatter.LotteryDateTimeFormatter
import de.lotto24case.widget.model.WidgetDrawState
import de.lotto24case.widget.model.WidgetSupplementaryGame
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/**
 * Dedicated presentation mapper translating domain [LotteryDraw] objects into lightweight [WidgetDrawState]
 * models for Android Glance & iOS WidgetKit.
 */
object WidgetDrawMapper {

    /**
     * Localized label constants and formatters for widget layouts.
     */
    object Labels {
        const val LOTTO_6AUS49_MAIN = "GEWINNZAHLEN"
        const val LOTTO_6AUS49_SPECIAL = "Superzahl"
        const val LOTTO_6AUS49_ANCHOR = "6AUS49"
        fun lotto6aus49SpecialValue(superNumber: Int): String = "Superzahl: $superNumber"

        const val EUROJACKPOT_MAIN = "5 GEWINNZAHLEN"
        const val EUROJACKPOT_SPECIAL = "Eurozahlen"
        const val EUROJACKPOT_SPECIAL_VALUE = "2 EUROZAHLEN"
        const val EUROJACKPOT_ANCHOR = "EURO"

        const val UNIT_MILLION = "MIO"
        const val UNIT_THOUSAND = "Tsd"
        const val UNIT_CURRENCY = "€"
        const val FALLBACK_DATE = "–"
    }

    /**
     * Semantic holder for lottery-specific ball labels and numbers.
     */
    private data class WidgetBallLabels(
        val specialNumbers: List<Int>,
        val specialNumberLabel: String,
        val mainBallsLabel: String,
        val specialBallLabel: String?,
        val anchorLabel: String,
        val supplementaryGames: List<WidgetSupplementaryGame>
    )

    fun map(draw: LotteryDraw, isFixture: Boolean): WidgetDrawState {
        val ballLabels = when (draw) {
            is LotteryDraw.Lotto6aus49 -> WidgetBallLabels(
                specialNumbers = draw.superNumber?.let(::listOf).orEmpty(),
                specialNumberLabel = Labels.LOTTO_6AUS49_SPECIAL,
                mainBallsLabel = Labels.LOTTO_6AUS49_MAIN,
                specialBallLabel = draw.superNumber?.let { Labels.lotto6aus49SpecialValue(it) },
                anchorLabel = Labels.LOTTO_6AUS49_ANCHOR,
                supplementaryGames = draw.supplementaryLotteries.map { game ->
                    WidgetSupplementaryGame(
                        name = game.name,
                        winningNumber = game.winningNumber
                    )
                }
            )
            is LotteryDraw.Eurojackpot -> WidgetBallLabels(
                specialNumbers = draw.euroNumbers,
                specialNumberLabel = Labels.EUROJACKPOT_SPECIAL,
                mainBallsLabel = Labels.EUROJACKPOT_MAIN,
                specialBallLabel = Labels.EUROJACKPOT_SPECIAL_VALUE,
                anchorLabel = Labels.EUROJACKPOT_ANCHOR,
                supplementaryGames = emptyList()
            )
        }

        val fallbackZone = when (draw.lotteryType) {
            LotteryType.LOTTO_6AUS49 -> LotteryDateTimeFormatter.TIMEZONE_BERLIN
            LotteryType.EUROJACKPOT -> LotteryDateTimeFormatter.TIMEZONE_HELSINKI
        }
        val tz = if (!draw.timeZone.isNullOrBlank()) {
            LotteryDateTimeFormatter.resolveTimeZone(draw.timeZone)
        } else {
            fallbackZone
        }

        val formattedDate = LotteryDateTimeFormatter.formatDrawDate(draw.drawDate, tz) ?: Labels.FALLBACK_DATE
        val nextDrawText = LotteryDateTimeFormatter.formatNextDraw(draw.nextDrawDate, tz)
        val (jackpotAmountVal, jackpotUnitVal) = formatAnchorJackpot(draw.jackpot?.amount)
        val jackpotFormatted = CurrencyFormatter.formatJackpot(draw.jackpot?.amount)

        return WidgetDrawState(
            lotteryId = draw.lotteryType.id,
            title = draw.lotteryType.title,
            formattedDate = formattedDate,
            nextDrawText = nextDrawText,
            jackpotAmountValue = jackpotAmountVal,
            jackpotUnitValue = jackpotUnitVal,
            jackpotFormatted = jackpotFormatted,
            mainBallsLabel = ballLabels.mainBallsLabel,
            specialBallLabel = ballLabels.specialBallLabel,
            regularNumbers = draw.numbers.toPersistentList(),
            specialNumbers = ballLabels.specialNumbers.toPersistentList(),
            supplementaryGames = ballLabels.supplementaryGames.toPersistentList(),
            anchorLabel = ballLabels.anchorLabel,
            specialNumberLabel = ballLabels.specialNumberLabel,
            isFixture = isFixture
        )
    }

    fun formatAnchorJackpot(money: Money?): Pair<String?, String?> {
        if (money == null || money.minorUnits <= 0L) return null to null
        return when {
            money.minorUnits >= 100_000_000L -> {
                val millions = (money.minorUnits / 100_000_000L).toString()
                millions to Labels.UNIT_MILLION
            }
            money.minorUnits >= 100_000L -> {
                val thousands = (money.minorUnits / 100_000L).toString()
                thousands to Labels.UNIT_THOUSAND
            }
            else -> {
                val units = (money.minorUnits / 100L).toString()
                units to Labels.UNIT_CURRENCY
            }
        }
    }
}
