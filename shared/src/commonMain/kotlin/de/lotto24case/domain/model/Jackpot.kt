package de.lotto24case.domain.model

/**
 * Represents the jackpot height and currency for a lottery draw.
 *
 * Holds the raw amount as string (e.g. "80000000.00") matching the API payload
 * to prevent floating-point rounding errors and serialization precision loss.
 *
 * @property amount String representation of the jackpot amount (e.g. "80000000.00"), or null if unavailable
 * @property currency ISO currency string, default "EUR"
 * @property isSupported Whether jackpot calculation is currently supported by the provider
 */
data class Jackpot(
    val amount: String?,
    val currency: String = "EUR",
    val isSupported: Boolean = true
) {
    val hasAmount: Boolean
        get() = isSupported && !amount.isNullOrBlank() && amount != "0.00" && amount != "0"
}
