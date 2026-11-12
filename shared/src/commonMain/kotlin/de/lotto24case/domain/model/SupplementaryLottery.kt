package de.lotto24case.domain.model

/**
 * Optional supplementary lottery games played alongside LOTTO 6aus49,
 * such as Spiel 77 (7 digits) or SUPER 6 (6 digits).
 *
 * Case Study Bonus: "Spiel77 and SUPER6 are also displayed."
 *
 * @property name Supplementary game name (e.g. "Spiel 77", "SUPER6")
 * @property winningNumber Formatted winning number string (e.g. "7312244", "112430")
 */
data class SupplementaryLottery(
    val name: String,
    val winningNumber: String
)
