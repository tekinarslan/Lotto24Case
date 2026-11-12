package de.lotto24case.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing past draw information and results.
 */
@Serializable
data class LastDrawDto(
    @SerialName("drawIdentifier")
    val drawIdentifier: String? = null,
    @SerialName("lottery")
    val lottery: String? = null,
    @SerialName("drawDate")
    val drawDate: String? = null,
    @SerialName("drawDateUtc")
    val drawDateUtc: String? = null,
    @SerialName("drawResult")
    val drawResult: DrawResultDto? = null,
    @SerialName("totalStake")
    val totalStake: String? = null,
    @SerialName("currency")
    val currency: String? = "EUR"
)
