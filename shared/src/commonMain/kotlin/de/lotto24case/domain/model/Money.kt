package de.lotto24case.domain.model

/**
 * Value object representing a monetary amount in minor units (e.g. cents for EUR).
 *
 * Prevents floating-point rounding errors and keeps the domain layer purely semantic
 * without currency symbols, abbreviations (MIO), or localized presentation strings.
 *
 * @property minorUnits Amount in the currency's smallest unit (e.g. 80_000_000.00 EUR = 8_000_000_000 cents).
 * @property currency Currency specification, defaults to [Currency.EUR].
 */
data class Money(
    val minorUnits: Long,
    val currency: Currency = Currency.EUR
) : Comparable<Money> {

    init {
        require(minorUnits >= 0) { "Monetary amount cannot be negative: $minorUnits" }
    }

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Cannot compare money with different currencies: $currency vs ${other.currency}" }
        return minorUnits.compareTo(other.minorUnits)
    }

    companion object {
        private const val CENTS_PER_UNIT = 100L

        /**
         * Parses a string amount (e.g. "80000000.00", "5029008.30", "1500000") into a [Money] value object.
         *
         * Avoids floating-point [Double] operations in KMP commonMain by directly parsing the whole and fractional
         * components using integer arithmetic, padding/truncating decimal cents to 2 digits, and checking for Long overflow.
         *
         * Returns null if string is null, blank, zero, negative, or unparseable.
         */
        fun fromMajorUnitsString(amountStr: String?, currency: Currency = Currency.EUR): Money? {
            if (amountStr.isNullOrBlank()) return null
            val trimmed = amountStr.trim().replace(" ", "")

            // Normalize decimal separators (support German "10.000.000,00" or US "10,000,000.00" or plain "80000000.00")
            val normalized = if (trimmed.contains(',') && trimmed.contains('.')) {
                if (trimmed.lastIndexOf(',') > trimmed.lastIndexOf('.')) {
                    // German style: strip dots, convert comma to dot
                    trimmed.replace(".", "").replace(',', '.')
                } else {
                    // US style: strip commas
                    trimmed.replace(",", "")
                }
            } else if (trimmed.contains(',')) {
                trimmed.replace(',', '.')
            } else {
                trimmed
            }

            if (normalized.startsWith("-")) return null

            val parts = normalized.split('.')
            if (parts.size > 2) return null

            val integerPartStr = parts[0].ifEmpty { "0" }
            val fractionalPartStr = if (parts.size == 2) parts[1] else "0"

            val wholeUnits = integerPartStr.toLongOrNull() ?: return null
            if (wholeUnits < 0) return null

            // Pad or truncate fractional cents to exactly 2 digits:
            // "5" -> "50", "30" -> "30", "305" -> "30", "" -> "00"
            val centsStr = fractionalPartStr.padEnd(2, '0').take(2)
            val cents = centsStr.toLongOrNull() ?: return null
            if (cents < 0) return null

            // Overflow protection during multiplication by 100
            val maxWholeUnits = (Long.MAX_VALUE - cents) / CENTS_PER_UNIT
            if (wholeUnits > maxWholeUnits) return null

            val totalMinorUnits = (wholeUnits * CENTS_PER_UNIT) + cents
            if (totalMinorUnits <= 0L) return null

            return Money(minorUnits = totalMinorUnits, currency = currency)
        }

        fun fromMajorUnits(amount: Long, currency: Currency = Currency.EUR): Money {
            require(amount >= 0) { "Amount cannot be negative: $amount" }
            return Money(minorUnits = amount * CENTS_PER_UNIT, currency = currency)
        }
    }
}
