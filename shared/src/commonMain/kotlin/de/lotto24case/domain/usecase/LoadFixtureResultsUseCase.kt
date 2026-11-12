package de.lotto24case.domain.usecase

import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.repository.LotteryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for explicitly loading sample fixture draw results upon user request (e.g. from 404 error screen).
 *
 * @property repository The underlying [LotteryRepository] abstraction.
 */
class LoadFixtureResultsUseCase(
    private val repository: LotteryRepository
) {
    /**
     * Executes the use case to load embedded sample fixture results.
     *
     * @return Flow emitting [LotteryResult].
     */
    operator fun invoke(): Flow<LotteryResult> {
        return repository.loadFixtureResults()
    }
}
