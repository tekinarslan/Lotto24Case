package de.lotto24case.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class LotteryDrawModelTest {

    @Test
    fun `lotto6aus49 correctly associates type and raw numbers without duplicating balls`() {
        val draw = LotteryDraw.Lotto6aus49(
            drawDate = Instant.parse("2023-01-21T18:25:00Z"),
            drawIdentifier = "2023-01-21",
            nextDrawDate = Instant.parse("2023-01-25T17:25:00Z"),
            nextDrawIdentifier = "2023-01-25",
            jackpot = Jackpot(amount = Money.fromMajorUnitsString("6000000.00"), isSupported = true),
            totalStake = Money.fromMajorUnitsString("44712847.20"),
            numbers = listOf(4, 19, 24, 43, 44, 45),
            superNumber = 7,
            supplementaryLotteries = listOf(
                SupplementaryLottery("Spiel 77", "7312244"),
                SupplementaryLottery("SUPER6", "112430")
            )
        )

        assertEquals(LotteryType.LOTTO_6AUS49, draw.lotteryType)
        assertEquals(6, draw.numbers.size)
        assertEquals(7, draw.superNumber)
        assertEquals(2, draw.supplementaryLotteries.size)
        assertEquals(Money.fromMajorUnitsString("6000000.00"), draw.jackpot?.amount)
    }

    @Test
    fun `eurojackpot correctly associates type and numbers`() {
        val draw = LotteryDraw.Eurojackpot(
            drawDate = Instant.parse("2023-01-20T18:00:00Z"),
            drawIdentifier = "2023-01-20",
            nextDrawDate = Instant.parse("2023-01-24T18:00:00Z"),
            nextDrawIdentifier = "2023-01-24",
            jackpot = Jackpot(amount = Money.fromMajorUnitsString("80000000.00"), isSupported = true),
            totalStake = Money.fromMajorUnitsString("54340762.00"),
            numbers = listOf(1, 12, 15, 31, 47),
            euroNumbers = listOf(9, 10)
        )

        assertEquals(LotteryType.EUROJACKPOT, draw.lotteryType)
        assertEquals(5, draw.numbers.size)
        assertEquals(2, draw.euroNumbers.size)
        assertEquals(listOf(9, 10), draw.euroNumbers)
        assertEquals(Money.fromMajorUnitsString("80000000.00"), draw.jackpot?.amount)
    }

    @Test
    fun `jackpot hasAmount returns true only when supported and greater than zero string`() {
        val activeJackpot = Jackpot(amount = Money.fromMajorUnitsString("15000000.00"), isSupported = true)
        val zeroDecimalJackpot = Jackpot(amount = Money.fromMajorUnitsString("0.00"), isSupported = true)
        val zeroIntJackpot = Jackpot(amount = Money.fromMajorUnitsString("0"), isSupported = true)
        val emptyJackpot = Jackpot(amount = Money.fromMajorUnitsString(""), isSupported = true)
        val nullJackpot = Jackpot(amount = null, isSupported = true)
        val unsupportedJackpot = Jackpot(amount = Money.fromMajorUnitsString("5000000.00"), isSupported = false)

        assertTrue(activeJackpot.hasAmount)
        assertFalse(zeroDecimalJackpot.hasAmount)
        assertFalse(zeroIntJackpot.hasAmount)
        assertFalse(emptyJackpot.hasAmount)
        assertFalse(nullJackpot.hasAmount)
        assertFalse(unsupportedJackpot.hasAmount)
    }

    @Test
    fun `lotteryType fromId resolves case-insensitively`() {
        assertEquals(LotteryType.LOTTO_6AUS49, LotteryType.fromId("6aus49"))
        assertEquals(LotteryType.LOTTO_6AUS49, LotteryType.fromId("6AUS49"))
        assertEquals(LotteryType.EUROJACKPOT, LotteryType.fromId("eurojackpot"))
        assertEquals(LotteryType.EUROJACKPOT, LotteryType.fromId("EUROJACKPOT"))
        assertNull(LotteryType.fromId("unknown_lottery"))
    }

    @Test
    fun `money fromMajorUnitsString parses precise integer cents without double precision errors`() {
        // Standard strings
        assertEquals(80_000_000_00L, Money.fromMajorUnitsString("80000000.00")?.minorUnits)
        assertEquals(5_029_008_30L, Money.fromMajorUnitsString("5029008.30")?.minorUnits)
        assertEquals(1_500_000_00L, Money.fromMajorUnitsString("1500000")?.minorUnits)

        // Fractional padding / truncating
        assertEquals(10_50L, Money.fromMajorUnitsString("10.5")?.minorUnits)
        assertEquals(10_55L, Money.fromMajorUnitsString("10.559")?.minorUnits)

        // German format with comma
        assertEquals(10_000_000_50L, Money.fromMajorUnitsString("10.000.000,50")?.minorUnits)

        // Invalid and zero values
        assertNull(Money.fromMajorUnitsString(null))
        assertNull(Money.fromMajorUnitsString(""))
        assertNull(Money.fromMajorUnitsString("0"))
        assertNull(Money.fromMajorUnitsString("0.00"))
        assertNull(Money.fromMajorUnitsString("-50.00"))
        assertNull(Money.fromMajorUnitsString("abc"))
    }
}
