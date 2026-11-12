package de.lotto24case.domain.util

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class LotteryDateTimeFormatterTest {

    @Test
    fun `formats draw date to localized German notation matching case study`() {
        // Mockup example: "Fr., 18.09.2020"
        val instant1 = Instant.parse("2020-09-18T18:00:00Z")
        val formatted1 = LotteryDateTimeFormatter.formatDrawDate(
            instant1,
            timeZone = LotteryDateTimeFormatter.TIMEZONE_BERLIN
        )
        assertEquals("Fr., 18.09.2020", formatted1)

        // Mockup example: "Mi., 23.09.2020"
        val instant2 = Instant.parse("2020-09-23T18:25:00Z")
        val formatted2 = LotteryDateTimeFormatter.formatDrawDate(
            instant2,
            timeZone = LotteryDateTimeFormatter.TIMEZONE_BERLIN
        )
        assertEquals("Mi., 23.09.2020", formatted2)
    }

    @Test
    fun `formats next draw badge prefix correctly`() {
        // Mockup example: "Nächste Ziehung: Mi., 23.09.2020"
        val instant = Instant.parse("2020-09-23T17:25:00Z")
        val formatted = LotteryDateTimeFormatter.formatNextDraw(
            instant,
            timeZone = LotteryDateTimeFormatter.TIMEZONE_BERLIN
        )
        assertEquals("Nächste Ziehung: Mi., 23.09.2020", formatted)
    }

    @Test
    fun `pads single-digit day and month with leading zeros`() {
        // 5th of January 2023 (Thursday)
        val instant = Instant.parse("2023-01-05T12:00:00Z")
        val formatted = LotteryDateTimeFormatter.formatDrawDate(instant)
        assertEquals("Do., 05.01.2023", formatted)
    }

    @Test
    fun `handles all German weekday abbreviations correctly`() {
        val mondays = listOf(
            "2023-01-02T12:00:00Z" to "Mo.",
            "2023-01-03T12:00:00Z" to "Di.",
            "2023-01-04T12:00:00Z" to "Mi.",
            "2023-01-05T12:00:00Z" to "Do.",
            "2023-01-06T12:00:00Z" to "Fr.",
            "2023-01-07T12:00:00Z" to "Sa.",
            "2023-01-08T12:00:00Z" to "So."
        )

        for ((iso, expectedDay) in mondays) {
            val formatted = LotteryDateTimeFormatter.formatDrawDate(Instant.parse(iso))
            val prefix = formatted?.substringBefore(",")
            assertEquals(expectedDay, prefix, "Failed for date: $iso")
        }
    }

    @Test
    fun `respects timezone boundary differences between Berlin and Helsinki`() {
        // At 22:30 UTC in winter (January 20, 2023):
        // Berlin (UTC+1) is 23:30 on Friday, Jan 20
        // Helsinki (UTC+2) is 00:30 on Saturday, Jan 21
        val edgeInstant = Instant.parse("2023-01-20T22:30:00Z")

        val berlinFormatted = LotteryDateTimeFormatter.formatDrawDate(
            edgeInstant,
            LotteryDateTimeFormatter.TIMEZONE_BERLIN
        )
        val helsinkiFormatted = LotteryDateTimeFormatter.formatDrawDate(
            edgeInstant,
            LotteryDateTimeFormatter.TIMEZONE_HELSINKI
        )

        assertEquals("Fr., 20.01.2023", berlinFormatted)
        assertEquals("Sa., 21.01.2023", helsinkiFormatted)
    }

    @Test
    fun `handles leap year date correctly`() {
        // Leap day: 29.02.2024 (Thursday)
        val leapDay = Instant.parse("2024-02-29T10:00:00Z")
        val formatted = LotteryDateTimeFormatter.formatDrawDate(leapDay)
        assertEquals("Do., 29.02.2024", formatted)
    }

    @Test
    fun `handles null instant gracefully`() {
        assertNull(LotteryDateTimeFormatter.formatDrawDate(null))
        assertNull(LotteryDateTimeFormatter.formatNextDraw(null))
    }

    @Test
    fun `resolves timezones safely with fallback to Berlin`() {
        assertEquals(
            TimeZone.of("Europe/Helsinki"),
            LotteryDateTimeFormatter.resolveTimeZone("Europe/Helsinki")
        )
        assertEquals(
            LotteryDateTimeFormatter.TIMEZONE_BERLIN,
            LotteryDateTimeFormatter.resolveTimeZone(null)
        )
        assertEquals(
            LotteryDateTimeFormatter.TIMEZONE_BERLIN,
            LotteryDateTimeFormatter.resolveTimeZone("")
        )
        assertEquals(
            LotteryDateTimeFormatter.TIMEZONE_BERLIN,
            LotteryDateTimeFormatter.resolveTimeZone("Invalid/Zone_Name")
        )
    }
}
