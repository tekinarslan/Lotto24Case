package de.lotto24case.domain.repository

import de.lotto24case.domain.model.LotteryResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for lottery draw data.
 *
 * Adheres to Dependency Inversion: domain defines data expectations,
 * while data layer implements runtime in-memory caching and remote network calls.
 */
interface LotteryRepository {

    /**
     * Retrieves the latest draw results for all supported lotteries.
     *
     * @param forceRefresh When true, bypasses the in-memory cache and attempts a network fetch.
     * @return Flow emitting [LotteryResult] with live data, retained cache, or error.
     */
    fun getDrawResults(forceRefresh: Boolean = false): Flow<LotteryResult>

    /**
     * Explicitly loads sample PDF fixture data upon user demand (e.g. from 404 error view).
     *
     * @return Flow emitting [LotteryResult.Success] with `isFixture = true`.
     */
    fun loadFixtureResults(): Flow<LotteryResult>
}
