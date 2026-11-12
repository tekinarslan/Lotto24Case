package de.lotto24case.domain.usecase

import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.repository.LotteryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case encapsulating the retrieval of lottery draw results.
 *
 * @property repository The underlying [LotteryRepository] abstraction.
 */
class GetLotteryResultsUseCase(
    private val repository: LotteryRepository
) {
    /**
     * Executes the use case.
     *
     * @param forceRefresh Whether to force a fresh remote network fetch.
     * @return Flow emitting [LotteryResult].
     */
    operator fun invoke(forceRefresh: Boolean = false): Flow<LotteryResult> {
        return repository.getDrawResults(forceRefresh = forceRefresh)
    }
}
