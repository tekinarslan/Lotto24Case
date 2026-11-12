package de.lotto24case.domain.repository

import de.lotto24case.domain.model.LotteryDraw
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for lottery draw data.
 *
 * Adheres to Dependency Inversion: domain defines data expectations,
 * while data layer implements runtime memory caching and remote network calls.
 */
interface LotteryRepository {
    /**
     * Retrieves the latest draw results for all supported lotteries.
     *
     * @param forceRefresh When true, fetches fresh data from network.
     * @return Flow emitting [Result] wrapping the list of [LotteryDraw] entries.
     */
    fun getDrawResults(forceRefresh: Boolean = false): Flow<Result<List<LotteryDraw>>>
}
