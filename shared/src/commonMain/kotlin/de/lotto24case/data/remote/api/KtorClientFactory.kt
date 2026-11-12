package de.lotto24case.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured [HttpClient] instances across multiplatform targets.
 */
object KtorClientFactory {

    val defaultJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = false
        encodeDefaults = true
    }

    /**
     * Creates an [HttpClient] configured with JSON ContentNegotiation, Timeouts, and Logging.
     */
    fun create(
        json: Json = defaultJson,
        enableLogging: Boolean = true
    ): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 15_000L
            requestTimeoutMillis = 15_000L
            socketTimeoutMillis = 15_000L
        }

        if (enableLogging) {
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[Ktor] $message")
                    }
                }
            }
        }
    }
}
