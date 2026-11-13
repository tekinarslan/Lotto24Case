package de.lotto24case.data.mapper

import de.lotto24case.data.remote.dto.JackpotDto
import de.lotto24case.data.remote.dto.LotteryResponseDto
import de.lotto24case.domain.model.Currency
import de.lotto24case.domain.model.Jackpot
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.domain.model.Money
import de.lotto24case.domain.model.SupplementaryLottery
import kotlin.time.Instant

/**
 * Resilient DTO to Domain mapper.
 *
 * Requirements:
 * - Tolerates null or missing fields in API response.
 * - Extracts winning numbers without crash.
 * - Parses dates into ISO-8601 [Instant].
 * - Maps raw amounts into semantic [Money] value objects.
 */
object LotteryDtoMapper {

    fun mapResponseList(dtos: List<LotteryResponseDto>): List<LotteryDraw> {
        return dtos.mapNotNull { mapSingle(it) }
    }

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
        val timeZone = nextDraw?.timeZone ?: if (type == LotteryType.EUROJACKPOT) "Europe/Helsinki" else "Europe/Berlin"

        val currency = parseCurrency(lastDraw.currency ?: nextDraw?.jackpot?.currency)
        val jackpot = mapJackpot(nextDraw?.jackpot, currency)
        val totalStake = Money.fromMajorUnitsString(lastDraw.totalStake, currency)

        return when (type) {
            LotteryType.LOTTO_6AUS49 -> {
                LotteryDraw.Lotto6aus49(
                    drawDate = drawDate,
                    drawIdentifier = drawIdentifier,
                    nextDrawDate = nextDrawDate,
                    nextDrawIdentifier = nextDrawIdentifier,
                    timeZone = timeZone,
                    jackpot = jackpot,
                    totalStake = totalStake,
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
                    timeZone = timeZone,
                    jackpot = jackpot,
                    totalStake = totalStake,
                    numbers = numbers,
                    euroNumbers = drawResult.euroNumbers ?: emptyList()
                )
            }
        }
    }

    private fun mapJackpot(jackpotDto: JackpotDto?, currency: Currency): Jackpot? {
        if (jackpotDto == null) return null

        val amountStr = jackpotDto.jackpots?.get("WC_1")
        val isSupported = jackpotDto.jackpotSupported ?: (amountStr != null)
        val money = Money.fromMajorUnitsString(amountStr, currency)

        return Jackpot(
            amount = money,
            isSupported = isSupported
        )
    }

    private fun parseCurrency(currencyStr: String?): Currency {
        return when (currencyStr?.uppercase()) {
            "EUR" -> Currency.EUR
            else -> Currency.EUR
        }
    }

    /**
     * Extracts supplementary lottery games (Spiel 77 & SUPER6) dynamically from the DTO.
     * Returns an empty list if not present in the payload (no hardcoded mock values).
     */
    private fun extractSupplementaryLotteries(dto: LotteryResponseDto): List<SupplementaryLottery> {
        val result = dto.lastDraw?.drawResult ?: return emptyList()
        val list = mutableListOf<SupplementaryLottery>()

        if (!result.spiel77.isNullOrBlank()) {
            list.add(SupplementaryLottery(name = "Spiel 77", winningNumber = result.spiel77.trim()))
        }
        if (!result.super6.isNullOrBlank()) {
            list.add(SupplementaryLottery(name = "SUPER6", winningNumber = result.super6.trim()))
        }

        return list
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
