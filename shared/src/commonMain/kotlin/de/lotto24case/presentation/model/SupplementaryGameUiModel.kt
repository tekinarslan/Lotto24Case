package de.lotto24case.presentation.model

import androidx.compose.runtime.Immutable

/**
 * Immutable presentation model representing a supplementary lottery game (Spiel 77, SUPER6).
 *
 * Fully decoupled from domain [de.lotto24case.domain.model.SupplementaryLottery].
 *
 * @property name Name of the supplementary game (e.g. "Spiel 77", "SUPER6").
 * @property winningNumber Formatted winning string of digits.
 */
@Immutable
data class SupplementaryGameUiModel(
    val name: String,
    val winningNumber: String
)
