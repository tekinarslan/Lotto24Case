package de.lotto24case.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing jackpot details from the API.
 *
 * Jackpot winning classes (e.g. "WC_1", "WC_2") are delivered as a Map of strings
 * (e.g. {"WC_1": "80000000.00"}).
 */
@Serializable
data class JackpotDto(
    @SerialName("drawIdentifier")
    val drawIdentifier: String? = null,
    @SerialName("lottery")
    val lottery: String? = null,
    @SerialName("drawDate")
    val drawDate: String? = null,
    @SerialName("currency")
    val currency: String? = "EUR",
    @SerialName("jackpots")
    val jackpots: Map<String, String>? = null,
    @SerialName("jackpotSupported")
    val jackpotSupported: Boolean? = null
)
