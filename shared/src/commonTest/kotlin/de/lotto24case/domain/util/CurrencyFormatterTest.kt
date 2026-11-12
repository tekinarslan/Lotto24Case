package de.lotto24case.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrencyFormatterTest {

    @Test
    fun `formats exact millions from case study correctly`() {
        assertEquals("80 MIO €", CurrencyFormatter.formatJackpot("80000000.00"))
        assertEquals("6 MIO €", CurrencyFormatter.formatJackpot("6000000.00"))
        assertEquals("23 MIO €", CurrencyFormatter.formatJackpot("23000000.00"))
    }

    @Test
    fun `formats badge millions without euro sign`() {
        assertEquals("80 MIO", CurrencyFormatter.formatBadgeMio("80000000.00"))
        assertEquals("23 MIO", CurrencyFormatter.formatBadgeMio("23000000.00"))
        assertEquals("6 MIO", CurrencyFormatter.formatBadgeMio("6000000.00"))
    }

    @Test
    fun `formats decimal millions with German comma`() {
        assertEquals("1,5 MIO €", CurrencyFormatter.formatJackpot("1500000.00"))
        assertEquals("2,5 MIO €", CurrencyFormatter.formatJackpot("2500000.00"))
    }

    @Test
    fun `formats sub-million amounts with thousands dot`() {
        assertEquals("950.000 €", CurrencyFormatter.formatJackpot("950000.00"))
        assertEquals("50.000 €", CurrencyFormatter.formatJackpot("50000.00"))
        // Sub-million should not show MIO badge
        assertNull(CurrencyFormatter.formatBadgeMio("950000.00"))
    }

    @Test
    fun `handles edge cases and invalid amounts safely with null fallback`() {
        assertNull(CurrencyFormatter.formatJackpot(null))
        assertNull(CurrencyFormatter.formatJackpot(""))
        assertNull(CurrencyFormatter.formatJackpot("   "))
        assertNull(CurrencyFormatter.formatJackpot("0.00"))
        assertNull(CurrencyFormatter.formatJackpot("0"))
        assertNull(CurrencyFormatter.formatJackpot("-1000000.00"))
        assertNull(CurrencyFormatter.formatJackpot("invalid_currency"))

        assertNull(CurrencyFormatter.formatBadgeMio(null))
        assertNull(CurrencyFormatter.formatBadgeMio("0.00"))
        assertNull(CurrencyFormatter.formatBadgeMio("abc"))
    }

    @Test
    fun `handles amounts with spaces or commas resiliently`() {
        assertEquals("80 MIO €", CurrencyFormatter.formatJackpot(" 80000000.00 "))
        assertEquals("10 MIO €", CurrencyFormatter.formatJackpot("10,000,000.00"))
    }
}
