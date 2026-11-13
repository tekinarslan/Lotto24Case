package de.lotto24case.domain.model

/**
 * Represents the jackpot height and state for a lottery draw.
 *
 * Holds the monetary amount as a semantic [Money] value object in the domain.
 *
 * @property amount Domain [Money] representation in minor units (e.g. cents), or null if unavailable
 * @property isSupported Whether jackpot calculation is currently supported by the provider
 */
data class Jackpot(
    val amount: Money?,
    val isSupported: Boolean = true
) {
    val hasAmount: Boolean
        get() = isSupported && amount != null && amount.minorUnits > 0L
}
