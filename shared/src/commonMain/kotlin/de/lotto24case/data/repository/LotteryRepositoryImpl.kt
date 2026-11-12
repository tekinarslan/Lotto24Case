package de.lotto24case.data.repository

import de.lotto24case.data.mapper.LotteryDtoMapper
import de.lotto24case.data.remote.api.Lotto24ApiService
import de.lotto24case.data.remote.model.LotteryDataResult
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.repository.LotteryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Concrete implementation of [LotteryRepository] managing remote API calls
 * and session-scoped in-memory caching.
 *
 * Requirements addressed:
 * - No persistent disk database (Room/SQLDelight) needed for lightweight 2-item lottery response.
 * - In-memory state retention: if network fails or 404 is encountered on pull-to-refresh,
 *   the previous successful draws are delivered in [LotteryResult.Error.cachedData] so UI
 *   can show existing data with an offline connection warning.
 * - Explicit fixture fallback: 404 does not silently pretend old fixture is live data.
 *   User must explicitly trigger [loadFixtureResults].
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

    private val inMemoryCache = MutableStateFlow<CacheEntry?>(null)

    override fun getDrawResults(forceRefresh: Boolean): Flow<LotteryResult> = flow {
        val cached = inMemoryCache.value
        if (!forceRefresh && cached != null) {
            emit(LotteryResult.Success(data = cached.data, isFixture = cached.isFixture))
            return@flow
        }

        when (val apiResult = apiService.getAggregatedDrawInfo()) {
            is LotteryDataResult.Live -> {
                val mapped = LotteryDtoMapper.mapResponseList(apiResult.data)
                inMemoryCache.value = CacheEntry(data = mapped, isFixture = false)
                emit(LotteryResult.Success(data = mapped, isFixture = false))
            }
            is LotteryDataResult.Fixture -> {
                val mapped = LotteryDtoMapper.mapResponseList(apiResult.data)
                inMemoryCache.value = CacheEntry(data = mapped, isFixture = true)
                emit(LotteryResult.Success(data = mapped, isFixture = true))
            }
            is LotteryDataResult.Error -> {
                emit(
                    LotteryResult.Error(
                        cause = apiResult.cause,
                        statusCode = apiResult.statusCode,
                        cachedData = cached?.data,
                        isCachedDataFixture = cached?.isFixture ?: false
                    )
                )
            }
        }
    }.flowOn(ioDispatcher)

    override fun loadFixtureResults(): Flow<LotteryResult> = flow {
        try {
            val fixtureDtos = apiService.getFixtureDrawInfo()
            val mapped = LotteryDtoMapper.mapResponseList(fixtureDtos)
            inMemoryCache.value = CacheEntry(data = mapped, isFixture = true)
            emit(LotteryResult.Success(data = mapped, isFixture = true))
        } catch (e: Exception) {
            val cached = inMemoryCache.value
            emit(
                LotteryResult.Error(
                    cause = e,
                    cachedData = cached?.data,
                    isCachedDataFixture = cached?.isFixture ?: false
                )
            )
        }
    }.flowOn(ioDispatcher)

    /**
     * Diagnostic helper for testing: returns current in-memory cached draws.
     */
    fun currentCachedData(): List<LotteryDraw>? = inMemoryCache.value?.data

    /**
     * Diagnostic helper for testing: returns whether current cache is fixture data.
     */
    fun isCacheFixture(): Boolean = inMemoryCache.value?.isFixture == true

    /**
     * Clears session cache (useful for testing or full user reset).
     */
    fun clearCache() {
        inMemoryCache.value = null
    }
}
