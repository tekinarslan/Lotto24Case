package de.lotto24case.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing winning numbers for a draw.
 *
 * LOTTO 6aus49 returns [numbers] and single [superNumber].
 * Eurojackpot returns [numbers] and list [euroNumbers].
 */
@Serializable
data class DrawResultDto(
    @SerialName("numbers")
    val numbers: List<Int> = emptyList(),
    @SerialName("superNumber")
    val superNumber: Int? = null,
    @SerialName("euroNumbers")
    val euroNumbers: List<Int>? = null
)
