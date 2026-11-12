package de.lotto24case.data.repository

import app.cash.turbine.test
import de.lotto24case.data.remote.api.Lotto24ApiService
import de.lotto24case.data.remote.dto.DrawResultDto
import de.lotto24case.data.remote.dto.LastDrawDto
import de.lotto24case.data.remote.dto.LotteryResponseDto
import de.lotto24case.data.remote.model.LotteryDataResult
import de.lotto24case.domain.model.LotteryResult
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LotteryRepositoryImplTest {

    private class FakeApiService : Lotto24ApiService {
        var apiResultToReturn: LotteryDataResult = LotteryDataResult.Live(emptyList())
        var fixtureToReturn: List<LotteryResponseDto> = emptyList()
        var fetchCallCount: Int = 0
        var fixtureCallCount: Int = 0

        override suspend fun getAggregatedDrawInfo(lotteries: String): LotteryDataResult {
            fetchCallCount++
            return apiResultToReturn
        }

        override fun getFixtureDrawInfo(): List<LotteryResponseDto> {
            fixtureCallCount++
            return fixtureToReturn
        }
    }

    private val sampleLotto6aus49Dto = LotteryResponseDto(
        lottery = "6aus49",
        lastDraw = LastDrawDto(
            drawIdentifier = "2023-01-21",
            drawDateUtc = "2023-01-21T18:25:00Z",
            drawResult = DrawResultDto(
                numbers = listOf(4, 19, 24, 43, 44, 45),
                superNumber = 7,
                spiel77 = "7312244",
                super6 = "112430"
            )
        )
    )

    @Test
    fun `emits live data on success and updates in-memory cache`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val fakeApi = FakeApiService().apply {
            apiResultToReturn = LotteryDataResult.Live(listOf(sampleLotto6aus49Dto))
        }
        val repository = LotteryRepositoryImpl(fakeApi, testDispatcher)

        repository.getDrawResults(forceRefresh = false).test {
            val item = awaitItem()
            assertTrue(item is LotteryResult.Success)
            assertEquals(1, item.data.size)
            assertEquals(false, item.isFixture)
            awaitComplete()
        }

        assertEquals(1, fakeApi.fetchCallCount)
        assertNotNull(repository.currentCachedData())
        assertEquals(1, repository.currentCachedData()?.size)
    }

    @Test
    fun `serves cached data directly without network call when forceRefresh is false`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val fakeApi = FakeApiService().apply {
            apiResultToReturn = LotteryDataResult.Live(listOf(sampleLotto6aus49Dto))
        }
        val repository = LotteryRepositoryImpl(fakeApi, testDispatcher)

        // First call fetches from network
        repository.getDrawResults(forceRefresh = false).test {
            awaitItem()
            awaitComplete()
        }
        assertEquals(1, fakeApi.fetchCallCount)

        // Second call with forceRefresh = false serves from memory cache
        repository.getDrawResults(forceRefresh = false).test {
            val item = awaitItem()
            assertTrue(item is LotteryResult.Success)
            assertEquals(1, item.data.size)
            awaitComplete()
        }
        // Network call count must NOT increase
        assertEquals(1, fakeApi.fetchCallCount)
    }

    @Test
    fun `bypasses in-memory cache when forceRefresh is true`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val fakeApi = FakeApiService().apply {
            apiResultToReturn = LotteryDataResult.Live(listOf(sampleLotto6aus49Dto))
        }
        val repository = LotteryRepositoryImpl(fakeApi, testDispatcher)

        // Initial fetch
        repository.getDrawResults(forceRefresh = false).test {
            awaitItem()
            awaitComplete()
        }
        assertEquals(1, fakeApi.fetchCallCount)

        // Pull to refresh forces remote call
        repository.getDrawResults(forceRefresh = true).test {
            awaitItem()
            awaitComplete()
        }
        assertEquals(2, fakeApi.fetchCallCount)
    }

    @Test
    fun `emits error with null cachedData on initial 404 failure`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val fakeApi = FakeApiService().apply {
            apiResultToReturn = LotteryDataResult.Error(
                cause = IllegalStateException("HTTP 404: Not Found"),
                statusCode = 404
            )
        }
        val repository = LotteryRepositoryImpl(fakeApi, testDispatcher)

        repository.getDrawResults(forceRefresh = false).test {
            val item = awaitItem()
            assertTrue(item is LotteryResult.Error)
            assertEquals(404, item.statusCode)
            assertNull(item.cachedData)
            awaitComplete()
        }
    }

    @Test
    fun `retains previous in-memory data in error when network fails on refresh (offline support)`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val fakeApi = FakeApiService().apply {
            apiResultToReturn = LotteryDataResult.Live(listOf(sampleLotto6aus49Dto))
        }
        val repository = LotteryRepositoryImpl(fakeApi, testDispatcher)

        // 1. Initial successful fetch
        repository.getDrawResults(forceRefresh = false).test {
            val item = awaitItem()
            assertTrue(item is LotteryResult.Success)
            awaitComplete()
        }

        // 2. Network disconnects (e.g. offline on pull-to-refresh)
        fakeApi.apiResultToReturn = LotteryDataResult.Error(
            cause = IllegalStateException("No internet connection"),
            statusCode = null
        )

        repository.getDrawResults(forceRefresh = true).test {
            val item = awaitItem()
            assertTrue(item is LotteryResult.Error)
            // Crucial: UI receives previous cachedData to display offline warning banner!
            assertNotNull(item.cachedData)
            assertEquals(1, item.cachedData.size)
            awaitComplete()
        }
    }

    @Test
    fun `explicitly loads fixture results and marks isFixture true`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val fakeApi = FakeApiService().apply {
            fixtureToReturn = listOf(sampleLotto6aus49Dto)
        }
        val repository = LotteryRepositoryImpl(fakeApi, testDispatcher)

        repository.loadFixtureResults().test {
            val item = awaitItem()
            assertTrue(item is LotteryResult.Success)
            assertTrue(item.isFixture)
            assertEquals(1, item.data.size)
            awaitComplete()
        }

        assertEquals(1, fakeApi.fixtureCallCount)
        assertNotNull(repository.currentCachedData())
        assertTrue(repository.isCacheFixture())
    }

    @Test
    fun `preserves isFixture true provenance when serving cached fixture on subsequent getDrawResults`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val fakeApi = FakeApiService().apply {
            fixtureToReturn = listOf(sampleLotto6aus49Dto)
        }
        val repository = LotteryRepositoryImpl(fakeApi, testDispatcher)

        // 1. User explicitly loads fixture
        repository.loadFixtureResults().test {
            val item = awaitItem()
            assertTrue(item is LotteryResult.Success)
            assertTrue(item.isFixture)
            awaitComplete()
        }

        // 2. Later, app or screen requests draw results without forceRefresh
        repository.getDrawResults(forceRefresh = false).test {
            val cachedItem = awaitItem()
            assertTrue(cachedItem is LotteryResult.Success)
            // MUST PRESERVE isFixture = true! It must NOT falsely claim to be live!
            assertTrue(cachedItem.isFixture, "Cache hit must preserve isFixture = true to avoid spoofing live data")
            assertEquals(1, cachedItem.data.size)
            awaitComplete()
        }

        // 3. Network fails on a subsequent refresh: error must also indicate cached data is fixture!
        fakeApi.apiResultToReturn = LotteryDataResult.Error(
            cause = IllegalStateException("Network unreachable"),
            statusCode = 503
        )
        repository.getDrawResults(forceRefresh = true).test {
            val errorItem = awaitItem()
            assertTrue(errorItem is LotteryResult.Error)
            assertNotNull(errorItem.cachedData)
            assertTrue(errorItem.isCachedDataFixture, "Cached data in error must indicate it is fixture data")
            awaitComplete()
        }
    }
}
