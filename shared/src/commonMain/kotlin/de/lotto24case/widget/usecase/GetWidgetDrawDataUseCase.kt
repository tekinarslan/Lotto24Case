package de.lotto24case.widget.usecase

import de.lotto24case.domain.model.LotteryDraw
import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.domain.repository.LotteryRepository
import de.lotto24case.widget.mapper.WidgetDrawMapper
import de.lotto24case.widget.model.WidgetDrawState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * UseCase resolving lightweight [WidgetDrawState] items for home screen widgets.
 *
 * Guarantees:
 * 1. Strict Two-Game Contract: Widget always contains exactly two items (LOTTO 6aus49 and Eurojackpot).
 * 2. Deduplication & Ordering: Deduplicates repeated lottery draws and guarantees LOTTO 6aus49
 *    is always first (index 0) and Eurojackpot is always second (index 1).
 * 3. Graceful Missing Lottery Filling: If the primary source only returned one lottery or an empty list,
 *    the missing lottery is automatically fetched and populated from local fixtures.
 * 4. Provenance Tracking: Accurately preserves whether data is live or fixture (including cached states).
 * 5. Returns [ImmutableList] to enforce Compose stability across presentation consumers.
 */
class GetWidgetDrawDataUseCase(
    private val repository: LotteryRepository
) {

    suspend operator fun invoke(forceRefresh: Boolean = false): ImmutableList<WidgetDrawState> {
        // 1. Fetch from repository
        val (primaryDraws, primaryIsFixture) = when (val result = repository.getDrawResults(forceRefresh)) {
            is LotteryResult.Success -> {
                result.data to result.isFixture
            }
            is LotteryResult.Error -> {
                if (result.cachedData != null) {
                    result.cachedData to result.isCachedDataFixture
                } else {
                    emptyList<LotteryDraw>() to false
                }
            }
        }

        // 2. Deduplicate primary draws by lotteryType
        val distinctPrimary = primaryDraws.distinctBy { it.lotteryType }
        val has6aus49 = distinctPrimary.any { it.lotteryType == LotteryType.LOTTO_6AUS49 }
        val hasEurojackpot = distinctPrimary.any { it.lotteryType == LotteryType.EUROJACKPOT }

        // Map primary draws with their provenance
        val resolvedStates = mutableListOf<WidgetDrawState>()
        distinctPrimary.forEach { draw ->
            resolvedStates.add(WidgetDrawMapper.map(draw, isFixture = primaryIsFixture))
        }

        // 3. Fallback / Completion if either lottery is missing (guarantees exactly 2 games)
        if (!has6aus49 || !hasEurojackpot) {
            val fixtureDraws = when (val fixtureResult = repository.loadFixtureResults()) {
                is LotteryResult.Success -> fixtureResult.data
                is LotteryResult.Error -> fixtureResult.cachedData ?: emptyList()
            }

            if (!has6aus49) {
                fixtureDraws.find { it.lotteryType == LotteryType.LOTTO_6AUS49 }?.let { draw ->
                    resolvedStates.add(WidgetDrawMapper.map(draw, isFixture = true))
                }
            }

            if (!hasEurojackpot) {
                fixtureDraws.find { it.lotteryType == LotteryType.EUROJACKPOT }?.let { draw ->
                    resolvedStates.add(WidgetDrawMapper.map(draw, isFixture = true))
                }
            }
        }

        // 4. Normalize ordering: LOTTO 6aus49 (index 0) strictly before Eurojackpot (index 1)
        return resolvedStates.sortedBy { state ->
            when (state.lotteryId.lowercase()) {
                "6aus49", "lotto", "lotto_6aus49" -> 0
                else -> 1
            }
        }.toPersistentList()
    }
}
