package de.lotto24case.domain.model

import kotlin.time.Instant

/**
 * Polymorphic representation of a lottery draw result in the domain layer.
 *
 * Adheres to pure Kotlin Clean Architecture principles:
 * - Zero UI or Compose framework dependencies
 * - Single Source of Truth: does not duplicate drawn numbers into display lists
 * - Strictly differentiates 6aus49 (superNumber) from Eurojackpot (euroNumbers)
 */
sealed interface LotteryDraw {
    val lotteryType: LotteryType
    val drawDate: Instant
    val drawIdentifier: String
    val nextDrawDate: Instant?
    val nextDrawIdentifier: String?
    val timeZone: String?
    val jackpot: Jackpot?
    val totalStake: String?
    val currency: String
    val numbers: List<Int>

    /**
     * German LOTTO 6aus49 draw result.
     *
     * Consists of:
     * - 6 regular numbers drawn from 1..49
     * - 1 optional Super Number (Superzahl) from 0..9
     * - Optional supplementary lottery numbers (Spiel 77, SUPER6)
     */
    data class Lotto6aus49(
        override val drawDate: Instant,
        override val drawIdentifier: String,
        override val nextDrawDate: Instant?,
        override val nextDrawIdentifier: String?,
        override val timeZone: String? = "Europe/Berlin",
        override val jackpot: Jackpot?,
        override val totalStake: String?,
        override val currency: String = "EUR",
        override val numbers: List<Int>,
        val superNumber: Int?,
        val supplementaryLotteries: List<SupplementaryLottery> = emptyList()
    ) : LotteryDraw {
        override val lotteryType: LotteryType = LotteryType.LOTTO_6AUS49
    }

    /**
     * Pan-European Eurojackpot draw result.
     *
     * Consists of:
     * - 5 regular numbers drawn from 1..50
     * - 2 Euro Numbers (Eurozahlen) drawn from 1..12
     */
    data class Eurojackpot(
        override val drawDate: Instant,
        override val drawIdentifier: String,
        override val nextDrawDate: Instant?,
        override val nextDrawIdentifier: String?,
        override val timeZone: String? = "Europe/Helsinki",
        override val jackpot: Jackpot?,
        override val totalStake: String?,
        override val currency: String = "EUR",
        override val numbers: List<Int>,
        val euroNumbers: List<Int>
    ) : LotteryDraw {
        override val lotteryType: LotteryType = LotteryType.EUROJACKPOT
    }
}
