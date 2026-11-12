package de.lotto24case.domain.util

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Utility for formatting lottery draw dates according to German locale standards.
 *
 * Output examples from UX mockup:
 * - "Fr., 18.09.2020"
 * - "Mi., 23.09.2020"
 * - "Nächste Ziehung: Mi., 23.09.2020"
 */
object LotteryDateTimeFormatter {

    val TIMEZONE_BERLIN: TimeZone = TimeZone.of("Europe/Berlin")
    val TIMEZONE_HELSINKI: TimeZone = TimeZone.of("Europe/Helsinki")

    /**
     * Formats an [Instant] into German draw date notation (e.g. "Fr., 18.09.2020").
     *
     * @param instant The timestamp of the draw.
     * @param timeZone Target timezone (defaults to Europe/Berlin).
     * @return Formatted string, or null if instant is null.
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
