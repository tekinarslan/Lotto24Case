package de.lotto24case.domain.util

import kotlin.math.roundToLong

/**
 * Utility for formatting lottery monetary amounts according to German locale standards.
 *
 * Requirements:
 * - 80,000,000.00 -> "80 MIO €" or "80 MIO" for badges
 * - 6,000,000.00 -> "6 MIO €"
 * - 1,500,000.00 -> "1,5 MIO €" (German decimal comma)
 * - 950,000.00 -> "950.000 €" (sub-million handling)
 * - 0.00, empty, null, or negative -> null (safe fallback)
 */
object CurrencyFormatter {

    private const val ONE_MILLION = 1_000_000.0

    /**
     * Formats a raw jackpot string (e.g. "80000000.00") into a human-readable German string.
     *
     * @param amount The raw string amount from API payload.
     * @param includeCurrency Whether to append " €" (defaults to true).
     * @return Formatted string like "80 MIO €", or null if the amount is invalid, zero, or unavailable.
     */
    fun formatJackpot(
        amount: String?,
        includeCurrency: Boolean = true
    ): String? {
        val parsed = parseAmount(amount) ?: return null
        if (parsed <= 0.0) return null

        val formattedText = if (parsed >= ONE_MILLION) {
            val millions = parsed / ONE_MILLION
            val millionsRounded = (millions * 10).roundToLong() / 10.0
            if (millionsRounded % 1.0 == 0.0) {
                "${millionsRounded.toLong()} MIO"
            } else {
                "${millionsRounded.toString().replace('.', ',')} MIO"
            }
        } else {
            // Less than 1 million
            formatThousands(parsed.roundToLong())
        }

        return if (includeCurrency) "$formattedText €" else formattedText
    }

    /**
     * Formats specifically for the left-side card badge (e.g. "23 MIO", "80 MIO").
     *
     * Returns null if jackpot is 0 or unavailable, allowing UI to hide the badge gracefully.
     */
    fun formatBadgeMio(amount: String?): String? {
        val parsed = parseAmount(amount) ?: return null
        if (parsed < ONE_MILLION) return null

        val millions = (parsed / ONE_MILLION).roundToLong()
        return "$millions MIO"
    }

    private fun parseAmount(amountStr: String?): Double? {
        if (amountStr.isNullOrBlank()) return null
        val trimmed = amountStr.trim().replace(" ", "")

        val clean = if (trimmed.contains(',') && trimmed.contains('.')) {
            if (trimmed.lastIndexOf(',') > trimmed.lastIndexOf('.')) {
                // German style: 10.000.000,00 -> strip dots, replace comma with dot
                trimmed.replace(".", "").replace(',', '.')
            } else {
                // US style: 10,000,000.00 -> strip commas
                trimmed.replace(",", "")
            }
        } else if (trimmed.contains(',')) {
            trimmed.replace(',', '.')
        } else {
            trimmed
        }

        return clean.toDoubleOrNull()
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
