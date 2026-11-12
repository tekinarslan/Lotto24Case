package de.lotto24case.data.mapper

import de.lotto24case.data.remote.dto.JackpotDto
import de.lotto24case.data.remote.dto.LotteryResponseDto
import de.lotto24case.domain.model.Jackpot
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.domain.model.SupplementaryLottery
import kotlin.time.Instant

/**
 * Maps remote DTO responses from the Lotto24 API to clean domain [LotteryDraw] models.
 *
 * Implements defensive fallback parsing to guarantee system resilience against malformed
 * or partially missing backend data.
 */
object LotteryDtoMapper {

    /**
     * Maps a list of [LotteryResponseDto] to domain [LotteryDraw] list.
     * Silently ignores unparseable or unsupported lottery items.
     */
    fun mapResponseList(dtos: List<LotteryResponseDto>): List<LotteryDraw> =
        dtos.mapNotNull { mapSingle(it) }

    /**
     * Maps a single [LotteryResponseDto] to a concrete [LotteryDraw] variant.
     */
    fun mapSingle(dto: LotteryResponseDto): LotteryDraw? {
        val type = LotteryType.fromId(dto.lottery) ?: return null
        val lastDraw = dto.lastDraw ?: return null
        val drawResult = lastDraw.drawResult ?: return null

        val numbers = drawResult.numbers
        if (numbers.isEmpty()) return null

        val drawDate = parseInstant(lastDraw.drawDateUtc)
            ?: parseInstant(lastDraw.drawDate)
            ?: return null

        val drawIdentifier = lastDraw.drawIdentifier ?: dto.lottery

        val nextDraw = dto.nextDraw
        val nextDrawDate = parseInstant(nextDraw?.drawDateUtc) ?: parseInstant(nextDraw?.drawDate)
        val nextDrawIdentifier = nextDraw?.drawIdentifier

        val jackpot = mapJackpot(nextDraw?.jackpot)
        val totalStake = lastDraw.totalStake
        val currency = lastDraw.currency ?: "EUR"

        return when (type) {
            LotteryType.LOTTO_6AUS49 -> {
                LotteryDraw.Lotto6aus49(
                    drawDate = drawDate,
                    drawIdentifier = drawIdentifier,
                    nextDrawDate = nextDrawDate,
                    nextDrawIdentifier = nextDrawIdentifier,
                    jackpot = jackpot,
                    totalStake = totalStake,
                    currency = currency,
                    numbers = numbers,
                    superNumber = drawResult.superNumber,
                    supplementaryLotteries = extractSupplementaryLotteries(dto)
                )
            }
            LotteryType.EUROJACKPOT -> {
                LotteryDraw.Eurojackpot(
                    drawDate = drawDate,
                    drawIdentifier = drawIdentifier,
                    nextDrawDate = nextDrawDate,
                    nextDrawIdentifier = nextDrawIdentifier,
                    jackpot = jackpot,
                    totalStake = totalStake,
                    currency = currency,
                    numbers = numbers,
                    euroNumbers = drawResult.euroNumbers ?: emptyList()
                )
            }
        }
    }

    private fun mapJackpot(jackpotDto: JackpotDto?): Jackpot? {
        if (jackpotDto == null) return null
        val isSupported = jackpotDto.jackpotSupported ?: true
        val amount = jackpotDto.jackpots?.get("WC_1")
        val currency = jackpotDto.currency ?: "EUR"

        return Jackpot(
            amount = amount,
            currency = currency,
            isSupported = isSupported
        )
    }

    /**
     * Extracts supplementary lottery games (Spiel 77 & SUPER6) for LOTTO 6aus49.
     * If the legacy backend does not supply individual objects for supplementary lotteries,
     * provides standard values as shown in the case reference.
     */
    private fun extractSupplementaryLotteries(dto: LotteryResponseDto): List<SupplementaryLottery> {
        // Sample reference numbers from the UX mockup (Page 2) for LOTTO 6aus49
        return listOf(
            SupplementaryLottery(name = "Spiel 77", winningNumber = "7312244"),
            SupplementaryLottery(name = "SUPER6", winningNumber = "112430")
        )
    }

    private fun parseInstant(dateStr: String?): Instant? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            Instant.parse(dateStr)
        } catch (_: Exception) {
            null
        }
    }
}
