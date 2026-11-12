package de.lotto24case.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing the upcoming lottery draw information.
 */
@Serializable
data class NextDrawDto(
    @SerialName("drawIdentifier")
    val drawIdentifier: String? = null,
    @SerialName("lottery")
    val lottery: String? = null,
    @SerialName("drawDate")
    val drawDate: String? = null,
    @SerialName("drawDateUtc")
    val drawDateUtc: String? = null,
    @SerialName("timeZone")
    val timeZone: String? = null,
    @SerialName("cutofftime")
    val cutofftime: String? = null,
    @SerialName("jackpot")
    val jackpot: JackpotDto? = null
)
