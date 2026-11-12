package de.lotto24case.data.remote.model

import de.lotto24case.data.remote.dto.LotteryResponseDto

/**
 * Result abstraction for lottery network operations.
 *
 * Avoids silent fixture fallbacks so users are never misled by historical or sample draw data.
 */
sealed interface LotteryDataResult {
    /**
     * Successfully retrieved fresh, real-time data from the remote endpoint.
     */
    data class Live(val data: List<LotteryResponseDto>) : LotteryDataResult

    /**
     * Explicitly loaded sample/fixture data (e.g., when the user chooses to preview the app offline).
     */
    data class Fixture(val data: List<LotteryResponseDto>) : LotteryDataResult

    /**
     * Network or HTTP error (e.g. 404 Not Found, 500 Internal Server Error, connection lost).
     */
    data class Error(val cause: Throwable, val statusCode: Int? = null) : LotteryDataResult
}
