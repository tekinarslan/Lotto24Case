package de.lotto24case.presentation.mapper

import de.lotto24case.domain.model.Money
import de.lotto24case.domain.model.Jackpot
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.SupplementaryLottery
import de.lotto24case.presentation.model.BallUiType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class LotteryUiMapperTest {

    @Test
    fun `maps Lotto6aus49 domain model correctly into stable UI model with decoupled types`() {
        val domain = LotteryDraw.Lotto6aus49(
            drawDate = Instant.parse("2020-09-18T18:00:00Z"),
            drawIdentifier = "2020-09-18",
            nextDrawDate = Instant.parse("2020-09-23T18:25:00Z"),
            nextDrawIdentifier = "2020-09-23",
            timeZone = "Europe/Berlin",
            jackpot = Jackpot(amount = Money.fromMajorUnitsString("6000000.00")),
            totalStake = Money.fromMajorUnitsString("44712847.20"),
            numbers = listOf(4, 19, 24, 43, 44, 45),
            superNumber = 7,
            supplementaryLotteries = listOf(
                SupplementaryLottery("Spiel 77", "7312244"),
                SupplementaryLottery("SUPER6", "112430")
            )
        )

        val uiModel = LotteryUiMapper.mapSingle(domain)

        assertEquals("6aus49", uiModel.id)
        assertEquals("LOTTO 6aus49", uiModel.title)
        assertEquals("Fr., 18.09.2020", uiModel.formattedDrawDate)
        assertEquals("Nächste Ziehung: Mi., 23.09.2020", uiModel.nextDrawBadgeText)
        assertEquals("6 MIO €", uiModel.jackpotFormatted)
        assertEquals("6", uiModel.jackpotAmountValue)
        assertEquals("MIO", uiModel.jackpotUnitValue)
        assertTrue(uiModel.hasJackpot)

        // Verify balls: 6 regular + 1 superzahl mapped to UI types
        assertEquals(7, uiModel.balls.size)
        assertEquals(4, uiModel.balls[0].number)
        assertEquals(BallUiType.REGULAR, uiModel.balls[0].type)
        assertEquals(7, uiModel.balls[6].number)
        assertEquals(BallUiType.SUPER_ZAHL, uiModel.balls[6].type)

        // Verify supplementary games mapped to presentation models
        assertEquals(2, uiModel.supplementaryGames.size)
        assertEquals("Spiel 77", uiModel.supplementaryGames[0].name)
        assertEquals("7312244", uiModel.supplementaryGames[0].winningNumber)
    }

    @Test
    fun `maps Eurojackpot domain model with Eurozahlen and Helsinki timezone`() {
        // Instant at 22:30 UTC on Jan 20:
        // In Helsinki (UTC+2) it is already Jan 21 (Saturday)
        // In Berlin (UTC+1) it is still Jan 20 (Friday)
        val boundaryInstant = Instant.parse("2023-01-20T22:30:00Z")

        val eurojackpotDomain = LotteryDraw.Eurojackpot(
            drawDate = boundaryInstant,
            drawIdentifier = "2023-01-20",
            nextDrawDate = null,
            nextDrawIdentifier = null,
            timeZone = "Europe/Helsinki",
            jackpot = Jackpot(amount = Money.fromMajorUnitsString("80000000.00")),
            totalStake = Money.fromMajorUnitsString("54340762.00"),
            numbers = listOf(1, 12, 15, 31, 47),
            euroNumbers = listOf(9, 10)
        )

        val uiModel = LotteryUiMapper.mapSingle(eurojackpotDomain)

        // Explicitly tests that Helsinki timezone is used (Saturday, 21.01.2023)
        assertEquals("Sa., 21.01.2023", uiModel.formattedDrawDate)
        assertEquals("80 MIO €", uiModel.jackpotFormatted)
        assertEquals("80", uiModel.jackpotAmountValue)
        assertEquals("MIO", uiModel.jackpotUnitValue)

        // Verify balls: 5 regular + 2 eurozahlen
        assertEquals(7, uiModel.balls.size)
        assertEquals(BallUiType.REGULAR, uiModel.balls[0].type)
        assertEquals(BallUiType.REGULAR, uiModel.balls[4].type)
        assertEquals(9, uiModel.balls[5].number)
        assertEquals(BallUiType.EURO_ZAHL, uiModel.balls[5].type)
        assertEquals(10, uiModel.balls[6].number)
        assertEquals(BallUiType.EURO_ZAHL, uiModel.balls[6].type)

        assertTrue(uiModel.supplementaryGames.isEmpty())
    }

    @Test
    fun `handles null next draw date and missing jackpot safely`() {
        val domain = LotteryDraw.Lotto6aus49(
            drawDate = Instant.parse("2020-09-18T18:00:00Z"),
            drawIdentifier = "2020-09-18",
            nextDrawDate = null,
            nextDrawIdentifier = null,
            jackpot = null,
            totalStake = null,
            numbers = listOf(1, 2, 3, 4, 5, 6),
            superNumber = null,
            supplementaryLotteries = emptyList()
        )

        val uiModel = LotteryUiMapper.mapSingle(domain)

        assertEquals("Fr., 18.09.2020", uiModel.formattedDrawDate)
        assertNull(uiModel.nextDrawBadgeText)
        assertNull(uiModel.jackpotFormatted)
        assertNull(uiModel.jackpotAmountValue)
        assertNull(uiModel.jackpotUnitValue)
        assertEquals(false, uiModel.hasJackpot)
        assertEquals(6, uiModel.balls.size)
    }
}
