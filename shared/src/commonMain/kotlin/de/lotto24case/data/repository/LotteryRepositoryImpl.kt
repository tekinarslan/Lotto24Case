package de.lotto24case.data.repository

import de.lotto24case.data.mapper.LotteryDtoMapper
import de.lotto24case.data.remote.api.Lotto24ApiService
import de.lotto24case.data.remote.model.LotteryDataResult
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.repository.LotteryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of [LotteryRepository] managing remote API calls
 * and session-scoped in-memory caching using clean one-shot suspend functions.
 *
 * @property apiService The network service abstraction.
 * @property ioDispatcher Coroutine dispatcher for background processing.
 */
class LotteryRepositoryImpl(
    private val apiService: Lotto24ApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : LotteryRepository {

    /**
     * Session cache entry preserving both the draw models and the data source provenance.
     */
    private data class CacheEntry(
        val data: List<LotteryDraw>,
        val isFixture: Boolean
    )

    @kotlin.concurrent.Volatile
    private var inMemoryCache: CacheEntry? = null

    override suspend fun getDrawResults(forceRefresh: Boolean): LotteryResult = withContext(ioDispatcher) {
        val cached = inMemoryCache
        if (!forceRefresh && cached != null) {
            return@withContext LotteryResult.Success(data = cached.data, isFixture = cached.isFixture)
        }

        when (val apiResult = apiService.getAggregatedDrawInfo()) {
            is LotteryDataResult.Live -> {
                val mapped = LotteryDtoMapper.mapResponseList(apiResult.data)
                inMemoryCache = CacheEntry(data = mapped, isFixture = false)
                LotteryResult.Success(data = mapped, isFixture = false)
            }
            is LotteryDataResult.Fixture -> {
                val mapped = LotteryDtoMapper.mapResponseList(apiResult.data)
                inMemoryCache = CacheEntry(data = mapped, isFixture = true)
                LotteryResult.Success(data = mapped, isFixture = true)
            }
            is LotteryDataResult.Error -> {
                LotteryResult.Error(
                    cause = apiResult.cause,
                    statusCode = apiResult.statusCode,
                    cachedData = cached?.data,
                    isCachedDataFixture = cached?.isFixture ?: false
                )
            }
        }
    }

    override suspend fun loadFixtureResults(): LotteryResult = withContext(ioDispatcher) {
        try {
            val fixtureDtos = apiService.getFixtureDrawInfo()
            val mapped = LotteryDtoMapper.mapResponseList(fixtureDtos)
            inMemoryCache = CacheEntry(data = mapped, isFixture = true)
            LotteryResult.Success(data = mapped, isFixture = true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val cached = inMemoryCache
            LotteryResult.Error(
                cause = e,
                cachedData = cached?.data,
                isCachedDataFixture = cached?.isFixture ?: false
            )
        }
    }

    /**
     * Diagnostic helper for testing: returns current in-memory cached draws.
     */
    fun currentCachedData(): List<LotteryDraw>? = inMemoryCache?.data

    /**
     * Diagnostic helper for testing: returns whether current cache is fixture data.
     */
    fun isCacheFixture(): Boolean = inMemoryCache?.isFixture == true

    /**
     * Clears session cache (useful for testing or full user reset).
     */
    fun clearCache() {
        inMemoryCache = null
    }
}
