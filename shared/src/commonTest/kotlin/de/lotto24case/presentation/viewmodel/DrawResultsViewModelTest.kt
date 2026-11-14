package de.lotto24case.presentation.viewmodel

import app.cash.turbine.test
import de.lotto24case.domain.model.Money
import de.lotto24case.domain.model.Jackpot
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.repository.LotteryRepository
import de.lotto24case.domain.usecase.GetLotteryResultsUseCase
import de.lotto24case.domain.usecase.LoadFixtureResultsUseCase
import de.lotto24case.presentation.state.DrawResultsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DrawResultsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeRepository : LotteryRepository {
        var resultToReturn: LotteryResult = LotteryResult.Success(emptyList())
        var fixtureToReturn: LotteryResult = LotteryResult.Success(emptyList())
        var lastForceRefresh: Boolean? = null
        var fixtureCalled: Boolean = false

        override suspend fun getDrawResults(forceRefresh: Boolean): LotteryResult {
            lastForceRefresh = forceRefresh
            return resultToReturn
        }

        override suspend fun loadFixtureResults(): LotteryResult {
            fixtureCalled = true
            return fixtureToReturn
        }
    }

    private val sampleDraw = LotteryDraw.Lotto6aus49(
        drawDate = Instant.parse("2023-01-21T18:25:00Z"),
        drawIdentifier = "2023-01-21",
        nextDrawDate = Instant.parse("2023-01-25T18:25:00Z"),
        nextDrawIdentifier = "2023-01-25",
        jackpot = Jackpot(amount = Money.fromMajorUnitsString("6000000.00")),
        totalStake = Money.fromMajorUnitsString("44712847.20"),
        numbers = listOf(4, 19, 24, 43, 44, 45),
        superNumber = 7,
        supplementaryLotteries = emptyList()
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load emits loading then success with live data`() = runTest {
        val repo = FakeRepository().apply {
            resultToReturn = LotteryResult.Success(data = listOf(sampleDraw), isFixture = false)
        }
        val getUseCase = GetLotteryResultsUseCase(repo)
        val fixtureUseCase = LoadFixtureResultsUseCase(repo)

        val viewModel = DrawResultsViewModel(getUseCase, fixtureUseCase)

        viewModel.uiState.test {
            assertEquals(DrawResultsUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val success = awaitItem() as DrawResultsUiState.Success
            assertEquals(1, success.draws.size)
            assertEquals(false, success.isFixture)
            assertEquals(false, success.isOfflineFallback)
            assertNull(success.bannerMessage)
        }
    }

    @Test
    fun `canLoadFixture is true across error scenarios including 404, 500, and network outages`() = runTest {
        val repo = FakeRepository().apply {
            resultToReturn = LotteryResult.Error(
                cause = IllegalStateException("Not Found"),
                statusCode = 404,
                cachedData = null
            )
        }
        val getUseCase = GetLotteryResultsUseCase(repo)
        val fixtureUseCase = LoadFixtureResultsUseCase(repo)

        val viewModel = DrawResultsViewModel(getUseCase, fixtureUseCase)

        viewModel.uiState.test {
            assertEquals(DrawResultsUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val error404 = awaitItem() as DrawResultsUiState.Error
            assertEquals(404, error404.statusCode)
            assertTrue(error404.canLoadFixture, "404 must allow loading fixtures")
            assertEquals(DrawResultsViewModel.ERROR_HTTP_404, error404.message)

            // Case 2: 500 -> canLoadFixture must also be true
            repo.resultToReturn = LotteryResult.Error(
                cause = IllegalStateException("Internal Server Error"),
                statusCode = 500,
                cachedData = null
            )
            viewModel.onRetryClicked()
            assertEquals(DrawResultsUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val error500 = awaitItem() as DrawResultsUiState.Error
            assertEquals(500, error500.statusCode)
            assertTrue(error500.canLoadFixture, "500 server error must offer fixture loading")
            assertEquals(DrawResultsViewModel.ERROR_SERVER_ERROR, error500.message)

            // Case 3: Network exception (no status code) -> canLoadFixture must also be true
            repo.resultToReturn = LotteryResult.Error(
                cause = IllegalStateException("No internet connection"),
                statusCode = null,
                cachedData = null
            )
            viewModel.onRetryClicked()
            assertEquals(DrawResultsUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val errorNetwork = awaitItem() as DrawResultsUiState.Error
            assertNull(errorNetwork.statusCode)
            assertTrue(errorNetwork.canLoadFixture, "Network outage must offer fixture loading")
            assertEquals(DrawResultsViewModel.ERROR_NO_CONNECTION, errorNetwork.message)
        }
    }

    @Test
    fun `pull to refresh updates isRefreshing flag and refreshes data`() = runTest {
        val repo = FakeRepository().apply {
            resultToReturn = LotteryResult.Success(listOf(sampleDraw))
        }
        val getUseCase = GetLotteryResultsUseCase(repo)
        val fixtureUseCase = LoadFixtureResultsUseCase(repo)

        val viewModel = DrawResultsViewModel(getUseCase, fixtureUseCase)

        viewModel.uiState.test {
            assertEquals(DrawResultsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Success

            // Trigger pull to refresh
            viewModel.onRefresh()
            val refreshingState = awaitItem() as DrawResultsUiState.Success
            assertTrue(refreshingState.isRefreshing)

            testDispatcher.scheduler.advanceUntilIdle()
            val finishedState = awaitItem() as DrawResultsUiState.Success
            assertEquals(false, finishedState.isRefreshing)
        }
    }

    @Test
    fun `onLoadFixtureClicked loads sample fixture and sets isFixture true with banner`() = runTest {
        val repo = FakeRepository().apply {
            resultToReturn = LotteryResult.Error(IllegalStateException("404"), statusCode = 404)
            fixtureToReturn = LotteryResult.Success(listOf(sampleDraw), isFixture = true)
        }
        val getUseCase = GetLotteryResultsUseCase(repo)
        val fixtureUseCase = LoadFixtureResultsUseCase(repo)

        val viewModel = DrawResultsViewModel(getUseCase, fixtureUseCase)

        viewModel.uiState.test {
            assertEquals(DrawResultsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Error state

            // User taps "Beispieldaten laden"
            viewModel.onLoadFixtureClicked()
            assertEquals(DrawResultsUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val fixtureSuccess = awaitItem() as DrawResultsUiState.Success
            assertTrue(fixtureSuccess.isFixture)
            assertEquals(DrawResultsViewModel.BANNER_FIXTURE, fixtureSuccess.bannerMessage)
            assertEquals(1, fixtureSuccess.draws.size)
        }
    }

    @Test
    fun `network failure during refresh preserves cached data and shows offline banner`() = runTest {
        val repo = FakeRepository().apply {
            resultToReturn = LotteryResult.Success(listOf(sampleDraw), isFixture = false)
        }
        val getUseCase = GetLotteryResultsUseCase(repo)
        val fixtureUseCase = LoadFixtureResultsUseCase(repo)

        val viewModel = DrawResultsViewModel(getUseCase, fixtureUseCase)

        viewModel.uiState.test {
            assertEquals(DrawResultsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Success

            // Refresh fails with network outage, repository returns previous cached data
            repo.resultToReturn = LotteryResult.Error(
                cause = IllegalStateException("Network lost"),
                statusCode = null,
                cachedData = listOf(sampleDraw),
                isCachedDataFixture = false
            )

            viewModel.onRefresh()
            awaitItem() // refreshing = true

            testDispatcher.scheduler.advanceUntilIdle()

            val offlineSuccess = awaitItem() as DrawResultsUiState.Success
            assertTrue(offlineSuccess.isOfflineFallback)
            assertEquals(false, offlineSuccess.isFixture)
            assertEquals(DrawResultsViewModel.BANNER_OFFLINE_LIVE, offlineSuccess.bannerMessage)
            assertEquals(1, offlineSuccess.draws.size)
        }
    }
}
