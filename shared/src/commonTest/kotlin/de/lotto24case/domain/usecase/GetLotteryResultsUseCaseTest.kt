package de.lotto24case.domain.usecase

import de.lotto24case.domain.model.Money
import de.lotto24case.domain.model.Jackpot
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.repository.LotteryRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class GetLotteryResultsUseCaseTest {

    private class FakeLotteryRepository(
        private val result: LotteryResult
    ) : LotteryRepository {
        var lastForceRefresh: Boolean? = null
        var fixtureLoadCalled: Boolean = false

        override suspend fun getDrawResults(forceRefresh: Boolean): LotteryResult {
            lastForceRefresh = forceRefresh
            return result
        }

        override suspend fun loadFixtureResults(): LotteryResult {
            fixtureLoadCalled = true
            return result
        }
    }

    @Test
    fun `use case delegates to repository with forceRefresh flag`() = runTest {
        val sampleDraw = LotteryDraw.Lotto6aus49(
            drawDate = Instant.parse("2023-01-21T18:25:00Z"),
            drawIdentifier = "2023-01-21",
            nextDrawDate = null,
            nextDrawIdentifier = null,
            jackpot = Jackpot(amount = Money.fromMajorUnitsString("6000000.00")),
            totalStake = Money.fromMajorUnitsString("1000.00"),
            numbers = listOf(1, 2, 3, 4, 5, 6),
            superNumber = 7,
            supplementaryLotteries = emptyList()
        )
        val expectedList = listOf(sampleDraw)
        val fakeRepo = FakeLotteryRepository(LotteryResult.Success(expectedList))
        val useCase = GetLotteryResultsUseCase(fakeRepo)

        val emission = useCase(forceRefresh = true)
        assertTrue(emission is LotteryResult.Success)
        assertEquals(expectedList, emission.data)
        assertEquals(false, emission.isFixture)
        assertEquals(true, fakeRepo.lastForceRefresh)
    }

    @Test
    fun `use case propagates failure from repository with retained cache if present`() = runTest {
        val expectedException = IllegalStateException("Network unavailable")
        val fakeRepo = FakeLotteryRepository(
            LotteryResult.Error(
                cause = expectedException,
                statusCode = 503,
                cachedData = null
            )
        )
        val useCase = GetLotteryResultsUseCase(fakeRepo)

        val emission = useCase(forceRefresh = false)
        assertTrue(emission is LotteryResult.Error)
        assertEquals(expectedException.message, emission.cause.message)
        assertEquals(503, emission.statusCode)
        assertEquals(false, fakeRepo.lastForceRefresh)
    }

    @Test
    fun `load fixture use case delegates to repository`() = runTest {
        val fakeRepo = FakeLotteryRepository(LotteryResult.Success(emptyList(), isFixture = true))
        val useCase = LoadFixtureResultsUseCase(fakeRepo)

        val emission = useCase()
        assertTrue(emission is LotteryResult.Success)
        assertTrue(emission.isFixture)
        assertTrue(fakeRepo.fixtureLoadCalled)
    }
}
