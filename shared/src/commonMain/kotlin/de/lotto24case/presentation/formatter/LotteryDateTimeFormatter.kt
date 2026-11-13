package de.lotto24case.presentation.formatter

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Presentation-layer utility for formatting lottery draw dates according to German locale standards.
 *
 * Requirements:
 * - Output format: "Fr., 18.09.2020", "Mi., 23.09.2020"
 * - Badge output: "Nächste Ziehung: Mi., 23.09.2020"
 * - Dynamic remaining interval between draw and next draw: "X Tage Y Std." (e.g. "3 Tage 23 Std.")
 * - Domain never holds presentation strings.
 */
object LotteryDateTimeFormatter {

    val TIMEZONE_BERLIN: TimeZone = TimeZone.of("Europe/Berlin")
    val TIMEZONE_HELSINKI: TimeZone = TimeZone.of("Europe/Helsinki")

    /**
     * Formats an [Instant] into German draw date notation (e.g. "Fr., 18.09.2020").
     */
    fun formatDrawDate(
        instant: Instant?,
        timeZone: TimeZone = TIMEZONE_BERLIN
    ): String? {
        if (instant == null) return null
        val ldt = instant.toLocalDateTime(timeZone)
        val weekday = getGermanWeekday(ldt.dayOfWeek)
        val day = ldt.day.toString().padStart(2, '0')
        val month = (ldt.month.ordinal + 1).toString().padStart(2, '0')
        val year = ldt.year

        return "$weekday, $day.$month.$year"
    }

    /**
     * Formats the upcoming draw into badge notation (e.g. "Nächste Ziehung: Mi., 23.09.2020").
     */
    fun formatNextDraw(
        instant: Instant?,
        timeZone: TimeZone = TIMEZONE_BERLIN
    ): String? {
        val formattedDate = formatDrawDate(instant, timeZone) ?: return null
        return "Nächste Ziehung: $formattedDate"
    }

    /**
     * Calculates the remaining duration until the next draw as German interval pill text:
     * e.g. "3 Tage 23 Std.", "4 Tage 18 Std.", "14 Std. 30 Min."
     *
     * Calculated deterministically from API timestamps (nextDrawDate - drawDate) without runtime tickers.
     */
    fun formatRemainingTime(
        nextDrawDate: Instant?,
        drawDate: Instant?
    ): String? {
        if (nextDrawDate == null || drawDate == null) return null
        if (nextDrawDate <= drawDate) return null

        val duration: Duration = nextDrawDate - drawDate
        val totalSeconds = duration.inWholeSeconds
        if (totalSeconds <= 0) return null

        val days = totalSeconds / 86400
        val remainingSeconds = totalSeconds % 86400
        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60

        return when {
            days > 0 -> {
                val dayWord = if (days == 1L) "Tag" else "Tage"
                if (hours > 0) "$days $dayWord $hours Std." else "$days $dayWord"
            }
            hours > 0 -> {
                if (minutes > 0) "$hours Std. $minutes Min." else "$hours Std."
            }
            else -> {
                val safeMinutes = minutes.coerceAtLeast(1)
                "$safeMinutes Min."
            }
        }
    }

    /**
     * Resolves a timezone string with fallback to Berlin.
     */
    fun resolveTimeZone(zoneId: String?): TimeZone {
        if (zoneId.isNullOrBlank()) return TIMEZONE_BERLIN
        return try {
            TimeZone.of(zoneId)
        } catch (_: Exception) {
            TIMEZONE_BERLIN
        }
    }

    private fun getGermanWeekday(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Mo."
        DayOfWeek.TUESDAY -> "Di."
        DayOfWeek.WEDNESDAY -> "Mi."
        DayOfWeek.THURSDAY -> "Do."
        DayOfWeek.FRIDAY -> "Fr."
        DayOfWeek.SATURDAY -> "Sa."
        DayOfWeek.SUNDAY -> "So."
    }
}
