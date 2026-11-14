package de.lotto24case.data.remote.api

import de.lotto24case.data.remote.dto.LotteryResponseDto
import de.lotto24case.data.remote.fixture.Lotto24ApiFixtures
import de.lotto24case.data.remote.model.LotteryDataResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException

/**
 * Service interface for querying Lotto24 public drawinfo endpoints.
 */
interface Lotto24ApiService {
    /**
     * Attempts to fetch live data from the network.
     *
     * Returns [LotteryDataResult.Live] on success, or [LotteryDataResult.Error] on 4xx/5xx/connectivity failures.
     * Does NOT silently fall back to sample fixture so errors are visible to the user.
     *
     * @param lotteries Comma-separated list of lotteries (default: "6aus49,eurojackpot").
     * @return [LotteryDataResult] indicating the outcome.
     */
    suspend fun getAggregatedDrawInfo(
        lotteries: String = DEFAULT_LOTTERIES
    ): LotteryDataResult

    /**
     * Explicitly loads sample PDF fixture data upon user request (e.g. from 404 error screen).
     */
    fun getFixtureDrawInfo(): List<LotteryResponseDto>

    companion object {
        const val DEFAULT_BASE_URL = "https://www.lotto24.de"
        const val DEFAULT_LOTTERIES = "6aus49,eurojackpot"
        const val DRAW_INFO_PATH = "/public/v1/drawinfo/aggregated"
    }
}

/**
 * Concrete implementation of [Lotto24ApiService] using Ktor [HttpClient].
 */
class Lotto24ApiServiceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = Lotto24ApiService.DEFAULT_BASE_URL,
    private val fallbackJson: String = Lotto24ApiFixtures.PDF_SAMPLE_JSON
) : Lotto24ApiService {

    override suspend fun getAggregatedDrawInfo(lotteries: String): LotteryDataResult {
        val url = "$baseUrl${Lotto24ApiService.DRAW_INFO_PATH}/$lotteries"
        return try {
            val response: HttpResponse = httpClient.get(url)
            val statusCode = response.status.value
            if (statusCode in 200..299) {
                val data: List<LotteryResponseDto> = response.body()
                LotteryDataResult.Live(data)
            } else {
                LotteryDataResult.Error(
                    cause = IllegalStateException("HTTP $statusCode: ${response.status.description}"),
                    statusCode = statusCode
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LotteryDataResult.Error(cause = e)
        }
    }

    override fun getFixtureDrawInfo(): List<LotteryResponseDto> {
        return KtorClientFactory.defaultJson.decodeFromString(fallbackJson)
    }
}
