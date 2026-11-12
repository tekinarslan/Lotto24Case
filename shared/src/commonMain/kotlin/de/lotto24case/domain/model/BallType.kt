package de.lotto24case.domain.model

/**
 * Distinguishes visual styling and category of drawn lottery numbers.
 *
 * - [REGULAR]: Standard drawn numbers (1..49 or 1..50)
 * - [SUPER_ZAHL]: Special single number for LOTTO 6aus49 (0..9)
 * - [EURO_ZAHL]: Special two numbers for Eurojackpot (1..12)
 */
enum class BallType {
    REGULAR,
    SUPER_ZAHL,
    EURO_ZAHL
}
