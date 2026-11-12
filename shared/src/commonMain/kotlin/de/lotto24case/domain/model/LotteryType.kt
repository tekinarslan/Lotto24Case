package de.lotto24case.domain.model

/**
 * Supported lottery types in the application.
 *
 * @property id API identifier string (e.g., "6aus49", "eurojackpot")
 * @property title Human-readable brand title for display
 * @property regularNumbersCount Count of regular drawn balls (6 for 6aus49, 5 for Eurojackpot)
 * @property regularNumbersPool Total pool of regular numbers (1..49 or 1..50)
 */
enum class LotteryType(
    val id: String,
    val title: String,
    val regularNumbersCount: Int,
    val regularNumbersPool: Int
) {
    LOTTO_6AUS49(
        id = "6aus49",
        title = "LOTTO 6aus49",
        regularNumbersCount = 6,
        regularNumbersPool = 49
    ),
    EUROJACKPOT(
        id = "eurojackpot",
        title = "Eurojackpot",
        regularNumbersCount = 5,
        regularNumbersPool = 50
    );

    companion object {
        fun fromId(id: String): LotteryType? =
            entries.find { it.id.equals(id, ignoreCase = true) }
    }
}
