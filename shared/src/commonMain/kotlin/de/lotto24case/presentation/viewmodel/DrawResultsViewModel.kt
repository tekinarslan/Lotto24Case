package de.lotto24case.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.usecase.GetLotteryResultsUseCase
import de.lotto24case.domain.usecase.LoadFixtureResultsUseCase
import de.lotto24case.presentation.mapper.LotteryUiMapper
import de.lotto24case.presentation.state.DrawResultsUiState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrawResultsViewModel(
    private val getLotteryResultsUseCase: GetLotteryResultsUseCase,
    private val loadFixtureResultsUseCase: LoadFixtureResultsUseCase
) : ViewModel() {

    private var activeFetchJob: Job? = null

    private val _uiState = MutableStateFlow<DrawResultsUiState>(DrawResultsUiState.Loading)
    val uiState: StateFlow<DrawResultsUiState> = _uiState
        .onStart {
            if (_uiState.value is DrawResultsUiState.Loading && activeFetchJob == null) {
                launchRequest { getLotteryResultsUseCase(forceRefresh = false) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DrawResultsUiState.Loading
        )

    fun onRetryClicked() {
        _uiState.update { DrawResultsUiState.Loading }
        launchRequest { getLotteryResultsUseCase(forceRefresh = true) }
    }

    fun onRefresh() {
        val current = _uiState.value
        if (current is DrawResultsUiState.Loading) return
        if (current is DrawResultsUiState.Success && current.isRefreshing) return

        _uiState.update { state ->
            if (state is DrawResultsUiState.Success) state.copy(isRefreshing = true) else state
        }
        launchRequest { getLotteryResultsUseCase(forceRefresh = true) }
    }

    fun onLoadFixtureClicked() {
        val current = _uiState.value
        if (current !is DrawResultsUiState.Error || !current.canLoadFixture) return

        _uiState.update { DrawResultsUiState.Loading }
        launchRequest { loadFixtureResultsUseCase() }
    }

    /**
     * Single cancellation-safe execution pipeline:
     * 1. Cancels any in-flight request so stale responses never overwrite newer user intents.
     * 2. Propagates [CancellationException] strictly to preserve Structured Concurrency.
     * 3. Checks [ensureActive] before mutating UI state in case cancellation occurred right after I/O.
     */
    private fun launchRequest(block: suspend () -> LotteryResult) {
        activeFetchJob?.cancel()
        activeFetchJob = viewModelScope.launch {
            try {
                val result = block()
                // Guard against race conditions if cancellation occurred right as I/O completed
                ensureActive()
                handleResult(result)
            } catch (e: CancellationException) {
                // Never swallow CancellationException; rethrow to respect cooperative cancellation
                throw e
            } finally {
                if (activeFetchJob === coroutineContext[Job]) {
                    activeFetchJob = null
                }
            }
        }
    }

    private fun handleResult(result: LotteryResult) {
        when (result) {
            is LotteryResult.Success -> {
                _uiState.update {
                    DrawResultsUiState.Success(
                        draws = LotteryUiMapper.mapDraws(result.data).toPersistentList(),
                        isRefreshing = false,
                        isFixture = result.isFixture,
                        isOfflineFallback = false,
                        bannerMessage = if (result.isFixture) BANNER_FIXTURE else null
                    )
                }
            }

            is LotteryResult.Error -> {
                val cached = result.cachedData
                if (!cached.isNullOrEmpty()) {
                    _uiState.update {
                        DrawResultsUiState.Success(
                            draws = LotteryUiMapper.mapDraws(cached).toPersistentList(),
                            isRefreshing = false,
                            isFixture = result.isCachedDataFixture,
                            isOfflineFallback = true,
                            bannerMessage = if (result.isCachedDataFixture) {
                                BANNER_OFFLINE_FIXTURE
                            } else {
                                BANNER_OFFLINE_LIVE
                            }
                        )
                    }
                } else {
                    val errorMessage = when (result.statusCode) {
                        404 -> ERROR_HTTP_404
                        in 500..599 -> ERROR_SERVER_ERROR
                        else -> ERROR_NO_CONNECTION
                    }

                    _uiState.update {
                        DrawResultsUiState.Error(
                            message = errorMessage,
                            statusCode = result.statusCode,
                            canLoadFixture = true
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        const val BANNER_FIXTURE = "Beispieldaten aus der Fallstudie aktiv – keine Echtzeitdaten."
        const val BANNER_OFFLINE_LIVE = "Keine Internetverbindung – vorherige Ziehungsdaten werden angezeigt."
        const val BANNER_OFFLINE_FIXTURE = "Keine Internetverbindung – Beispieldaten werden angezeigt."

        const val ERROR_HTTP_404 = "Die Ziehungsergebnisse konnten nicht geladen werden (HTTP 404). Möchtest du Beispieldaten laden?"
        const val ERROR_SERVER_ERROR = "Der Server ist vorübergehend nicht erreichbar. Bitte versuche es später erneut."
        const val ERROR_NO_CONNECTION = "Keine Verbindung zum Server möglich. Bitte überprüfe deine Internetverbindung."
    }
}
