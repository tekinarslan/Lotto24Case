package de.lotto24case.data.mapper

import de.lotto24case.data.remote.api.KtorClientFactory
import de.lotto24case.data.remote.dto.DrawResultDto
import de.lotto24case.data.remote.dto.JackpotDto
import de.lotto24case.data.remote.dto.LastDrawDto
import de.lotto24case.data.remote.dto.LotteryResponseDto
import de.lotto24case.data.remote.dto.NextDrawDto
import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LotteryDtoMapperTest {

    private val sampleJsonFromPdf = """
    [
      {
        "lottery": "6aus49",
        "lastDraw": {
          "drawIdentifier": "2023-01-21",
          "lottery": "6aus49",
          "drawDate": "2023-01-21T19:25:00+01:00",
          "drawDateUtc": "2023-01-21T18:25:00Z",
          "drawResult": {
            "numbers": [4, 19, 24, 43, 44, 45],
            "superNumber": 7
          },
          "totalStake": "44712847.20",
          "currency": "EUR"
        },
        "nextDraw": {
          "drawIdentifier": "2023-01-25",
          "lottery": "6aus49",
          "drawDate": "2023-01-25T18:25:00+01:00",
          "drawDateUtc": "2023-01-25T17:25:00Z",
          "timeZone": "Europe/Berlin",
          "cutofftime": "2023-01-25T17:45:00+01:00",
          "jackpot": {
            "drawIdentifier": "2023-01-25",
            "lottery": "6aus49",
            "drawDate": "2023-01-25T18:25:00+01:00",
            "currency": "EUR",
            "jackpots": {
              "WC_1": "6000000.00",
              "WC_2": "1000000.00"
            },
            "jackpotSupported": true
          }
        },
        "draws": []
      },
      {
        "lottery": "eurojackpot",
        "lastDraw": {
          "drawIdentifier": "2023-01-20",
          "lottery": "eurojackpot",
          "drawDate": "2023-01-20T20:00:00+02:00",
          "drawDateUtc": "2023-01-20T18:00:00Z",
          "drawResult": {
            "numbers": [1, 12, 15, 31, 47],
            "euroNumbers": [9, 10]
          },
          "totalStake": "54340762.00",
          "currency": "EUR"
        },
        "nextDraw": {
          "drawIdentifier": "2023-01-24",
          "lottery": "eurojackpot",
          "drawDate": "2023-01-24T20:00:00+02:00",
          "drawDateUtc": "2023-01-24T18:00:00Z",
          "timeZone": "Europe/Helsinki",
          "cutofftime": "2023-01-24T19:35:00+02:00",
          "jackpot": {
            "drawIdentifier": "2023-01-24",
            "lottery": "eurojackpot",
            "drawDate": "2023-01-24T20:00:00+02:00",
            "currency": "EUR",
            "jackpots": {
              "WC_1": "80000000.00",
              "WC_2": "1000000.00"
            },
            "jackpotSupported": true
          }
        },
        "draws": []
      }
    ]
    """.trimIndent()

    @Test
    fun `maps full PDF json payload successfully into domain models`() {
        val dtos: List<LotteryResponseDto> = KtorClientFactory.defaultJson.decodeFromString(sampleJsonFromPdf)
        val domainDraws = LotteryDtoMapper.mapResponseList(dtos)

        assertEquals(2, domainDraws.size)

        // Verify 6aus49
        val lotto6aus49 = domainDraws.filterIsInstance<LotteryDraw.Lotto6aus49>().firstOrNull()
        assertNotNull(lotto6aus49)
        assertEquals(LotteryType.LOTTO_6AUS49, lotto6aus49.lotteryType)
        assertEquals(listOf(4, 19, 24, 43, 44, 45), lotto6aus49.numbers)
        assertEquals(7, lotto6aus49.superNumber)
        assertEquals("6000000.00", lotto6aus49.jackpot?.amount)
        assertTrue(lotto6aus49.jackpot?.hasAmount == true)
        assertEquals("44712847.20", lotto6aus49.totalStake)
        assertEquals(2, lotto6aus49.supplementaryLotteries.size)

        // Verify Eurojackpot
        val eurojackpot = domainDraws.filterIsInstance<LotteryDraw.Eurojackpot>().firstOrNull()
        assertNotNull(eurojackpot)
        assertEquals(LotteryType.EUROJACKPOT, eurojackpot.lotteryType)
        assertEquals(listOf(1, 12, 15, 31, 47), eurojackpot.numbers)
        assertEquals(listOf(9, 10), eurojackpot.euroNumbers)
        assertEquals("80000000.00", eurojackpot.jackpot?.amount)
        assertTrue(eurojackpot.jackpot?.hasAmount == true)
        assertEquals("54340762.00", eurojackpot.totalStake)
    }

    @Test
    fun `maps unsupported or null jackpot correctly`() {
        val dto = LotteryResponseDto(
            lottery = "6aus49",
            lastDraw = LastDrawDto(
                drawIdentifier = "2023-01-21",
                drawDateUtc = "2023-01-21T18:25:00Z",
                drawResult = DrawResultDto(numbers = listOf(1, 2, 3, 4, 5, 6))
            ),
            nextDraw = NextDrawDto(
                jackpot = JackpotDto(
                    jackpotSupported = false,
                    jackpots = emptyMap()
                )
            )
        )

        val draw = LotteryDtoMapper.mapSingle(dto) as? LotteryDraw.Lotto6aus49
        assertNotNull(draw)
        assertEquals(false, draw.jackpot?.isSupported)
        assertEquals(false, draw.jackpot?.hasAmount)
    }

    @Test
    fun `returns null when mandatory draw results are missing`() {
        val dtoMissingNumbers = LotteryResponseDto(
            lottery = "6aus49",
            lastDraw = LastDrawDto(
                drawDateUtc = "2023-01-21T18:25:00Z",
                drawResult = DrawResultDto(numbers = emptyList())
            )
        )
        assertNull(LotteryDtoMapper.mapSingle(dtoMissingNumbers))

        val dtoUnknownLottery = LotteryResponseDto(
            lottery = "super_unknown",
            lastDraw = LastDrawDto(
                drawDateUtc = "2023-01-21T18:25:00Z",
                drawResult = DrawResultDto(numbers = listOf(1, 2, 3))
            )
        )
        assertNull(LotteryDtoMapper.mapSingle(dtoUnknownLottery))
    }
}
