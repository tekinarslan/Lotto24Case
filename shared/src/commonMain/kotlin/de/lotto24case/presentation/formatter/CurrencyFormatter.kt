package de.lotto24case.presentation.formatter

import de.lotto24case.domain.model.Currency
import de.lotto24case.domain.model.Money
import kotlin.math.roundToLong

/**
 * Presentation-layer formatter for monetary values according to German lottery presentation standards.
 *
 * Requirements:
 * - 80,000,000.00 EUR -> "80 MIO €" or "80 MIO" for badges
 * - 6,000,000.00 EUR -> "6 MIO €"
 * - 1,500,000.00 EUR -> "1,5 MIO €" (German decimal comma)
 * - 950,000.00 EUR -> "950.000 €" (sub-million handling)
 * - null or zero -> null (safe fallback)
 */
object CurrencyFormatter {

    private const val CENTS_PER_MILLION = 100_000_000L // 1,000,000 * 100 cents
    private const val CENTS_PER_UNIT = 100L

    /**
     * Formats a domain [Money] object into a human-readable German string.
     *
     * @param money Domain monetary value.
     * @param includeCurrency Whether to append currency symbol (defaults to true).
     * @return Formatted string like "80 MIO €", or null if null or zero.
     */
    fun formatJackpot(
        money: Money?,
        includeCurrency: Boolean = true
    ): String? {
        if (money == null || money.minorUnits <= 0L) return null

        val formattedText = if (money.minorUnits >= CENTS_PER_MILLION) {
            val millionsDouble = money.minorUnits.toDouble() / CENTS_PER_MILLION
            val millionsRounded = (millionsDouble * 10).roundToLong() / 10.0
            if (millionsRounded % 1.0 == 0.0) {
                "${millionsRounded.toLong()} MIO"
            } else {
                "${millionsRounded.toString().replace('.', ',')} MIO"
            }
        } else {
            val majorUnits = money.minorUnits / CENTS_PER_UNIT
            formatThousands(majorUnits)
        }

        val symbol = getCurrencySymbol(money.currency)
        return if (includeCurrency) "$formattedText $symbol" else formattedText
    }

    /**
     * Convenience overload for raw strings (useful for backward compatibility in fixtures/tests).
     */
    fun formatJackpot(
        amountStr: String?,
        includeCurrency: Boolean = true,
        currency: Currency = Currency.EUR
    ): String? {
        val money = Money.fromMajorUnitsString(amountStr, currency) ?: return null
        return formatJackpot(money, includeCurrency)
    }

    /**
     * Formats specifically for the left-side card badge (e.g. "23 MIO", "80 MIO").
     * Returns null if jackpot is below 1 million or unavailable.
     */
    fun formatBadgeMio(money: Money?): String? {
        if (money == null || money.minorUnits < CENTS_PER_MILLION) return null
        val millions = (money.minorUnits.toDouble() / CENTS_PER_MILLION).roundToLong()
        return "$millions MIO"
    }

    /**
     * Convenience overload for raw strings.
     */
    fun formatBadgeMio(amountStr: String?, currency: Currency = Currency.EUR): String? {
        val money = Money.fromMajorUnitsString(amountStr, currency) ?: return null
        return formatBadgeMio(money)
    }

    private fun getCurrencySymbol(currency: Currency): String = when (currency) {
        Currency.EUR -> "€"
    }

    private fun formatThousands(value: Long): String {
        val str = value.toString()
        val builder = StringBuilder()
        var count = 0
        for (i in str.length - 1 downTo 0) {
            builder.append(str[i])
            count++
            if (count % 3 == 0 && i > 0) {
                builder.append('.')
            }
        }
        return builder.reverse().toString()
    }
}
