package de.lotto24case.widget

import de.lotto24case.domain.model.Currency
import de.lotto24case.domain.model.Jackpot
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.model.Money
import de.lotto24case.domain.model.SupplementaryLottery
import de.lotto24case.domain.repository.LotteryRepository
import de.lotto24case.widget.usecase.GetWidgetDrawDataUseCase
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetWidgetDrawDataUseCaseTest {

    private class FakeRepository : LotteryRepository {
        var drawResultToReturn: LotteryResult = LotteryResult.Success(emptyList())
        var fixtureResultToReturn: LotteryResult = LotteryResult.Success(emptyList())

        override suspend fun getDrawResults(forceRefresh: Boolean): LotteryResult = drawResultToReturn
        override suspend fun loadFixtureResults(): LotteryResult = fixtureResultToReturn
    }

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
    fun `maps successful live repository results to clean WidgetDrawState items with isFixture false`() = runTest {
        val repo = FakeRepository().apply {
            drawResultToReturn = LotteryResult.Success(listOf(sampleLotto6aus49, sampleEurojackpot), isFixture = false)
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size)

        // 6aus49
        val state1 = widgetStates[0]
        assertEquals("6aus49", state1.lotteryId)
        assertEquals("LOTTO 6aus49", state1.title)
        assertEquals("14", state1.jackpotAmountValue)
        assertEquals("MIO", state1.jackpotUnitValue)
        assertEquals("14 MIO €", state1.jackpotFormatted)
        assertEquals("6AUS49", state1.anchorLabel)
        assertEquals("Nächste Ziehung: Mi., 25.01.2023", state1.nextDrawText)
        assertEquals(2, state1.supplementaryGames.size)
        assertEquals("Spiel 77", state1.supplementaryGames[0].name)
        assertEquals("9415812", state1.supplementaryGames[0].winningNumber)
        assertEquals("SUPER6", state1.supplementaryGames[1].name)
        assertEquals("381144", state1.supplementaryGames[1].winningNumber)
        assertEquals("GEWINNZAHLEN", state1.mainBallsLabel)
        assertEquals("Superzahl: 7", state1.specialBallLabel)
        assertEquals(listOf(4, 19, 24, 43, 44, 45), state1.regularNumbers)
        assertEquals(listOf(7), state1.specialNumbers)
        assertEquals("Superzahl", state1.specialNumberLabel)
        assertFalse(state1.isFixture)

        // Eurojackpot
        val state2 = widgetStates[1]
        assertEquals("eurojackpot", state2.lotteryId)
        assertEquals("Eurojackpot", state2.title)
        assertEquals("45", state2.jackpotAmountValue)
        assertEquals("MIO", state2.jackpotUnitValue)
        assertEquals("45 MIO €", state2.jackpotFormatted)
        assertEquals("EURO", state2.anchorLabel)
        assertEquals("Nächste Ziehung: Di., 24.01.2023", state2.nextDrawText)
        assertTrue(state2.supplementaryGames.isEmpty())
        assertEquals("5 GEWINNZAHLEN", state2.mainBallsLabel)
        assertEquals("2 EUROZAHLEN", state2.specialBallLabel)
        assertEquals(listOf(3, 12, 18, 25, 41), state2.regularNumbers)
        assertEquals(listOf(2, 7), state2.specialNumbers)
        assertEquals("Eurozahlen", state2.specialNumberLabel)
        assertFalse(state2.isFixture)
    }

    @Test
    fun `preserves isFixture false when data is served from memory cache`() = runTest {
        val repo = FakeRepository().apply {
            drawResultToReturn = LotteryResult.Error(
                cause = IllegalStateException("Network unreachable"),
                statusCode = null,
                cachedData = listOf(sampleLotto6aus49, sampleEurojackpot),
                isCachedDataFixture = false
            )
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size)
        assertFalse(widgetStates[0].isFixture, "Live cached 6aus49 must retain isFixture = false")
        assertFalse(widgetStates[1].isFixture, "Live cached Eurojackpot must retain isFixture = false")
    }

    @Test
    fun `preserves isFixture true when cached data originated from sample fixture`() = runTest {
        val repo = FakeRepository().apply {
            drawResultToReturn = LotteryResult.Error(
                cause = IllegalStateException("No internet"),
                statusCode = null,
                cachedData = listOf(sampleLotto6aus49, sampleEurojackpot),
                isCachedDataFixture = true
            )
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size)
        assertTrue(widgetStates[0].isFixture, "Cached fixture 6aus49 must retain isFixture = true")
        assertTrue(widgetStates[1].isFixture, "Cached fixture Eurojackpot must retain isFixture = true")
    }

    @Test
    fun `populates missing Eurojackpot from fixture when repository only returns 6aus49`() = runTest {
        val repo = FakeRepository().apply {
            // Live repository only returns 6aus49
            drawResultToReturn = LotteryResult.Success(listOf(sampleLotto6aus49), isFixture = false)
            fixtureResultToReturn = LotteryResult.Success(listOf(sampleLotto6aus49, sampleEurojackpot), isFixture = true)
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size, "Widget must strictly contain both lotteries")

        // 6aus49 is from live source
        assertEquals("6aus49", widgetStates[0].lotteryId)
        assertFalse(widgetStates[0].isFixture)

        // Eurojackpot is populated from fixture fallback
        assertEquals("eurojackpot", widgetStates[1].lotteryId)
        assertTrue(widgetStates[1].isFixture, "Missing Eurojackpot filled from fixture must have isFixture = true")
    }

    @Test
    fun `populates missing 6aus49 from fixture when repository only returns Eurojackpot`() = runTest {
        val repo = FakeRepository().apply {
            // Live repository only returns Eurojackpot
            drawResultToReturn = LotteryResult.Success(listOf(sampleEurojackpot), isFixture = false)
            fixtureResultToReturn = LotteryResult.Success(listOf(sampleLotto6aus49, sampleEurojackpot), isFixture = true)
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size, "Widget must strictly contain both lotteries")

        // 6aus49 is populated from fixture fallback
        assertEquals("6aus49", widgetStates[0].lotteryId)
        assertTrue(widgetStates[0].isFixture, "Missing 6aus49 filled from fixture must have isFixture = true")

        // Eurojackpot is from live source
        assertEquals("eurojackpot", widgetStates[1].lotteryId)
        assertFalse(widgetStates[1].isFixture)
    }

    @Test
    fun `deduplicates repeated lottery draws and guarantees exactly two items`() = runTest {
        val repo = FakeRepository().apply {
            // Duplicate 6aus49 items returned
            drawResultToReturn = LotteryResult.Success(
                listOf(sampleLotto6aus49, sampleLotto6aus49, sampleEurojackpot, sampleEurojackpot),
                isFixture = false
            )
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size, "Must deduplicate lotteries so exactly two items are produced")
        assertEquals("6aus49", widgetStates[0].lotteryId)
        assertEquals("eurojackpot", widgetStates[1].lotteryId)
    }

    @Test
    fun `formats jackpot gracefully when null or zero`() = runTest {
        val drawNoJackpot = sampleLotto6aus49.copy(jackpot = null)
        val drawZeroJackpot = sampleEurojackpot.copy(
            jackpot = Jackpot(amount = Money(minorUnits = 0L, currency = Currency.EUR))
        )

        val repo = FakeRepository().apply {
            drawResultToReturn = LotteryResult.Success(listOf(drawNoJackpot, drawZeroJackpot), isFixture = false)
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size)

        assertNull(widgetStates[0].jackpotAmountValue)
        assertNull(widgetStates[0].jackpotUnitValue)

        assertNull(widgetStates[1].jackpotAmountValue)
        assertNull(widgetStates[1].jackpotUnitValue)
    }

    @Test
    fun `formats small jackpot in thousands and sub-thousands`() = runTest {
        val drawThousandJackpot = sampleLotto6aus49.copy(
            jackpot = Jackpot(amount = Money(minorUnits = 750_000_00L, currency = Currency.EUR)) // 750,000 €
        )
        val drawSubThousandJackpot = sampleEurojackpot.copy(
            jackpot = Jackpot(amount = Money(minorUnits = 450_00L, currency = Currency.EUR)) // 450 €
        )

        val repo = FakeRepository().apply {
            drawResultToReturn = LotteryResult.Success(listOf(drawThousandJackpot, drawSubThousandJackpot), isFixture = false)
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size)

        // 750,000 € -> "750" "Tsd"
        assertEquals("750", widgetStates[0].jackpotAmountValue)
        assertEquals("Tsd", widgetStates[0].jackpotUnitValue)

        // 450 € -> "450" "€"
        assertEquals("450", widgetStates[1].jackpotAmountValue)
        assertEquals("€", widgetStates[1].jackpotUnitValue)
    }

    @Test
    fun `populates both lotteries from fixture when repository returns empty list`() = runTest {
        val repo = FakeRepository().apply {
            drawResultToReturn = LotteryResult.Success(emptyList(), isFixture = false)
            fixtureResultToReturn = LotteryResult.Success(listOf(sampleLotto6aus49, sampleEurojackpot), isFixture = true)
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size)
        assertEquals("6aus49", widgetStates[0].lotteryId)
        assertTrue(widgetStates[0].isFixture)
        assertEquals("eurojackpot", widgetStates[1].lotteryId)
        assertTrue(widgetStates[1].isFixture)
    }

    @Test
    fun `normalizes ordering so that 6aus49 always strictly precedes eurojackpot`() = runTest {
        val repo = FakeRepository().apply {
            // Intentionally inverted repository response: Eurojackpot first, 6aus49 second
            drawResultToReturn = LotteryResult.Success(listOf(sampleEurojackpot, sampleLotto6aus49), isFixture = false)
        }
        val useCase = GetWidgetDrawDataUseCase(repo)

        val widgetStates = useCase()
        assertEquals(2, widgetStates.size)

        // Normalized order MUST put 6aus49 first
        assertEquals("6aus49", widgetStates[0].lotteryId)
        assertEquals("LOTTO 6aus49", widgetStates[0].title)

        // Eurojackpot MUST be second
        assertEquals("eurojackpot", widgetStates[1].lotteryId)
        assertEquals("Eurojackpot", widgetStates[1].title)
    }
}
