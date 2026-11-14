package de.lotto24case.widget

import de.lotto24case.domain.model.Currency
import de.lotto24case.domain.model.Jackpot
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.Money
import de.lotto24case.domain.model.SupplementaryLottery
import de.lotto24case.widget.mapper.WidgetDrawMapper
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WidgetDrawMapperTest {

    private val sampleLotto6aus49 = LotteryDraw.Lotto6aus49(
        drawDate = Instant.parse("2023-01-21T18:25:00Z"),
        drawIdentifier = "2023-01-21",
        nextDrawDate = Instant.parse("2023-01-25T17:25:00Z"),
        nextDrawIdentifier = "2023-01-25",
        timeZone = "Europe/Berlin",
        jackpot = Jackpot(amount = Money(minorUnits = 14_000_000_00L, currency = Currency.EUR)),
        totalStake = null,
        numbers = listOf(4, 19, 24, 43, 44, 45),
        superNumber = 7,
        supplementaryLotteries = listOf(
            SupplementaryLottery(name = "Spiel 77", winningNumber = "9415812"),
            SupplementaryLottery(name = "SUPER6", winningNumber = "381144")
        )
    )

    private val sampleEurojackpot = LotteryDraw.Eurojackpot(
        drawDate = Instant.parse("2023-01-20T19:00:00Z"),
        drawIdentifier = "2023-01-20",
        nextDrawDate = Instant.parse("2023-01-24T19:00:00Z"),
        nextDrawIdentifier = "2023-01-24",
        timeZone = "Europe/Helsinki",
        jackpot = Jackpot(amount = Money(minorUnits = 45_000_000_00L, currency = Currency.EUR)),
        totalStake = null,
        numbers = listOf(3, 12, 18, 25, 41),
        euroNumbers = listOf(2, 7)
    )

    @Test
    fun `maps Lotto6aus49 with semantic labels and formatted values`() {
        val state = WidgetDrawMapper.map(sampleLotto6aus49, isFixture = true)

        assertEquals("6aus49", state.lotteryId)
        assertEquals("LOTTO 6aus49", state.title)
        assertEquals(WidgetDrawMapper.Labels.LOTTO_6AUS49_MAIN, state.mainBallsLabel)
        assertEquals(WidgetDrawMapper.Labels.LOTTO_6AUS49_SPECIAL, state.specialNumberLabel)
        assertEquals("Superzahl: 7", state.specialBallLabel)
        assertEquals(listOf(4, 19, 24, 43, 44, 45), state.regularNumbers)
        assertEquals(listOf(7), state.specialNumbers)
        assertEquals("14", state.jackpotAmountValue)
        assertEquals("MIO", state.jackpotUnitValue)
        assertEquals("14 MIO €", state.jackpotFormatted)
        assertEquals("6AUS49", state.anchorLabel)
        assertEquals("Nächste Ziehung: Mi., 25.01.2023", state.nextDrawText)
        assertEquals(2, state.supplementaryGames.size)
        assertEquals("Spiel 77", state.supplementaryGames[0].name)
        assertEquals("9415812", state.supplementaryGames[0].winningNumber)
        assertEquals("SUPER6", state.supplementaryGames[1].name)
        assertEquals("381144", state.supplementaryGames[1].winningNumber)
        assertTrue(state.isFixture)
    }

    @Test
    fun `maps Eurojackpot with semantic labels and formatted values`() {
        val state = WidgetDrawMapper.map(sampleEurojackpot, isFixture = false)

        assertEquals("eurojackpot", state.lotteryId)
        assertEquals("Eurojackpot", state.title)
        assertEquals(WidgetDrawMapper.Labels.EUROJACKPOT_MAIN, state.mainBallsLabel)
        assertEquals(WidgetDrawMapper.Labels.EUROJACKPOT_SPECIAL, state.specialNumberLabel)
        assertEquals(WidgetDrawMapper.Labels.EUROJACKPOT_SPECIAL_VALUE, state.specialBallLabel)
        assertEquals(listOf(3, 12, 18, 25, 41), state.regularNumbers)
        assertEquals(listOf(2, 7), state.specialNumbers)
        assertEquals("45", state.jackpotAmountValue)
        assertEquals("MIO", state.jackpotUnitValue)
        assertEquals("45 MIO €", state.jackpotFormatted)
        assertEquals("EURO", state.anchorLabel)
        assertEquals("Nächste Ziehung: Di., 24.01.2023", state.nextDrawText)
        assertTrue(state.supplementaryGames.isEmpty())
    }

    @Test
    fun `formatAnchorJackpot correctly formats million thousand sub-thousand and null values`() {
        val millions = Money(14_000_000_00L, Currency.EUR)
        val thousands = Money(750_000_00L, Currency.EUR)
        val subThousands = Money(450_00L, Currency.EUR)
        val zero = Money(0L, Currency.EUR)

        assertEquals("14" to "MIO", WidgetDrawMapper.formatAnchorJackpot(millions))
        assertEquals("750" to "Tsd", WidgetDrawMapper.formatAnchorJackpot(thousands))
        assertEquals("450" to "€", WidgetDrawMapper.formatAnchorJackpot(subThousands))
        assertEquals(null to null, WidgetDrawMapper.formatAnchorJackpot(zero))
        assertEquals(null to null, WidgetDrawMapper.formatAnchorJackpot(null))
    }
}
