package de.lotto24case.domain.model

/**
 * Domain-level result of a lottery data fetch operation.
 *
 * Explicitly distinguishes between live network data, sample fixtures,
 * and errors with optional retained in-memory data for offline presentation.
 */
sealed interface LotteryResult {

    /**
     * Successfully retrieved lottery draws.
     *
     * @property data The mapped domain draws.
     * @property isFixture True if data originated from local sample fixtures (e.g. after explicit 404 fallback).
     */
    data class Success(
        val data: List<LotteryDraw>,
        val isFixture: Boolean = false
    ) : LotteryResult

    /**
     * Failed retrieval.
     *
     * @property cause The underlying exception (network timeout, connectivity failure, etc.).
     * @property statusCode Optional HTTP status code (e.g. 404, 500) if a response was received.
     * @property cachedData Retained in-memory draw data from an earlier successful fetch in this session, if available.
     *                      Allows the UI to keep displaying previously loaded draws with an offline banner.
     * @property isCachedDataFixture Indicates whether the retained [cachedData] originated from sample fixtures.
     */
    data class Error(
        val cause: Throwable,
        val statusCode: Int? = null,
        val cachedData: List<LotteryDraw>? = null,
        val isCachedDataFixture: Boolean = false
    ) : LotteryResult
}
