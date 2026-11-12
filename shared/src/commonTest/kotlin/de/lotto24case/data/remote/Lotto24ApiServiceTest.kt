package de.lotto24case.data.remote

import de.lotto24case.data.remote.api.KtorClientFactory
import de.lotto24case.data.remote.api.Lotto24ApiServiceImpl
import de.lotto24case.data.remote.model.LotteryDataResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class Lotto24ApiServiceTest {

    private val sampleJson = """
    [
      {
        "lottery": "6aus49",
        "lastDraw": {
          "drawIdentifier": "2023-01-21",
          "drawDateUtc": "2023-01-21T18:25:00Z",
          "drawResult": {
            "numbers": [4, 19, 24, 43, 44, 45],
            "superNumber": 7
          }
        }
      }
    ]
    """.trimIndent()

    @Test
    fun `getAggregatedDrawInfo returns Live result on 200 OK`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/public/v1/drawinfo/aggregated/6aus49,eurojackpot", request.url.encodedPath)
            respond(
                content = sampleJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(KtorClientFactory.defaultJson)
            }
        }

        val apiService = Lotto24ApiServiceImpl(httpClient = client, baseUrl = "https://mock.lotto24.de")
        val result = apiService.getAggregatedDrawInfo()

        assertIs<LotteryDataResult.Live>(result)
        assertEquals(1, result.data.size)
        assertEquals("6aus49", result.data.first().lottery)
        assertEquals(listOf(4, 19, 24, 43, 44, 45), result.data.first().lastDraw?.drawResult?.numbers)
    }

    @Test
    fun `getAggregatedDrawInfo returns Error on 404 without silent fallback`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(KtorClientFactory.defaultJson)
            }
        }

        val apiService = Lotto24ApiServiceImpl(httpClient = client, baseUrl = "https://mock.lotto24.de")
        val result = apiService.getAggregatedDrawInfo()

        assertIs<LotteryDataResult.Error>(result)
        assertEquals(404, result.statusCode)
    }

    @Test
    fun `getAggregatedDrawInfo returns Error on 500 error`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(KtorClientFactory.defaultJson)
            }
        }

        val apiService = Lotto24ApiServiceImpl(httpClient = client, baseUrl = "https://mock.lotto24.de")
        val result = apiService.getAggregatedDrawInfo()

        assertIs<LotteryDataResult.Error>(result)
        assertEquals(500, result.statusCode)
    }

    @Test
    fun `getFixtureDrawInfo explicitly returns PDF fixture upon request`() {
        val client = HttpClient(MockEngine { respond("unused") })
        val apiService = Lotto24ApiServiceImpl(httpClient = client)

        val fixtures = apiService.getFixtureDrawInfo()
        assertEquals(2, fixtures.size)
        assertEquals("6aus49", fixtures[0].lottery)
        assertEquals("eurojackpot", fixtures[1].lottery)
    }
}
