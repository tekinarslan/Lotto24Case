package de.lotto24case.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level response item returned by https://www.lotto24.de/public/v1/drawinfo/aggregated/6aus49,eurojackpot
 */
@Serializable
data class LotteryResponseDto(
    @SerialName("lottery")
    val lottery: String,
    @SerialName("lastDraw")
    val lastDraw: LastDrawDto? = null,
    @SerialName("nextDraw")
    val nextDraw: NextDrawDto? = null,
    @SerialName("draws")
    val draws: List<LastDrawDto> = emptyList()
)
