package de.lotto24case.domain.repository

import de.lotto24case.domain.model.LotteryResult

/**
 * Domain repository contract for lottery draw data.
 *
 * Adheres to Dependency Inversion: domain defines data expectations,
 * while data layer implements runtime in-memory caching and remote network calls.
 * Uses suspend functions for one-shot data retrieval.
 */
interface LotteryRepository {

    /**
     * Retrieves the latest draw results for all supported lotteries.
     *
     * @param forceRefresh When true, bypasses the in-memory cache and attempts a network fetch.
     * @return [LotteryResult] with live data, retained cache, or error.
     */
    suspend fun getDrawResults(forceRefresh: Boolean = false): LotteryResult

    /**
     * Explicitly loads sample PDF fixture data upon user demand (e.g. from 404 error view).
     *
     * @return [LotteryResult.Success] with `isFixture = true`.
     */
    suspend fun loadFixtureResults(): LotteryResult
}
