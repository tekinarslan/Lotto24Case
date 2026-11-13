package de.lotto24case.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lotto24case.domain.model.LotteryResult
import de.lotto24case.domain.usecase.GetLotteryResultsUseCase
import de.lotto24case.domain.usecase.LoadFixtureResultsUseCase
import de.lotto24case.presentation.mapper.LotteryUiMapper
import de.lotto24case.presentation.state.DrawResultsUiState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating lottery draw UI states and user interactions.
 *
 * Implements clean Unidirectional Data Flow (UDF):
 * - Exposes single immutable [uiState] via [StateFlow].
 * - State-driven re-entrancy protection (guards against duplicate concurrent requests without manual Job management).
 * - Handles pull-to-refresh ([onRefresh]) and explicit fixture load from 404 screen ([onLoadFixtureClicked]).
 * - `canLoadFixture` is strictly guarded: only true for HTTP 404, false for 500 or network disconnects.
 * - Preserves previous in-memory draws when refresh fails due to network outage (offline banner).
 *
 * @property getLotteryResultsUseCase Use case retrieving live results from repository.
 * @property loadFixtureResultsUseCase Use case loading sample PDF fixtures upon explicit request.
 */
class DrawResultsViewModel(
    private val getLotteryResultsUseCase: GetLotteryResultsUseCase,
    private val loadFixtureResultsUseCase: LoadFixtureResultsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DrawResultsUiState>(DrawResultsUiState.Loading)
    val uiState: StateFlow<DrawResultsUiState> = _uiState.asStateFlow()

    init {
        loadData(forceRefresh = false)
    }

    /**
     * Triggered on initial screen display or manual retry.
     */
    fun onRetryClicked() {
        if (_uiState.value is DrawResultsUiState.Loading) return
        _uiState.value = DrawResultsUiState.Loading
        loadData(forceRefresh = true)
    }

    /**
     * Triggered during pull-to-refresh swipe.
     * Prevents duplicate requests if already refreshing or loading.
     */
    fun onRefresh() {
        val current = _uiState.value
        if (current is DrawResultsUiState.Loading || (current is DrawResultsUiState.Success && current.isRefreshing)) {
            return
        }
        if (current is DrawResultsUiState.Success) {
            _uiState.value = current.copy(isRefreshing = true)
        }
        loadData(forceRefresh = true)
    }

    /**
     * Explicitly loads sample fixture draws (e.g. from 404 error view).
     */
    fun onLoadFixtureClicked() {
        if (_uiState.value is DrawResultsUiState.Loading) return
        _uiState.value = DrawResultsUiState.Loading
        viewModelScope.launch {
            val result = loadFixtureResultsUseCase()
            handleResult(result)
        }
    }

    private fun loadData(forceRefresh: Boolean) {
        viewModelScope.launch {
            val result = getLotteryResultsUseCase(forceRefresh = forceRefresh)
            handleResult(result)
        }
    }

    private fun handleResult(result: LotteryResult) {
        when (result) {
            is LotteryResult.Success -> {
                val banner = if (result.isFixture) {
                    BANNER_FIXTURE
                } else {
                    null
                }

                _uiState.value = DrawResultsUiState.Success(
                    draws = LotteryUiMapper.mapDraws(result.data).toPersistentList(),
                    isRefreshing = false,
                    isFixture = result.isFixture,
                    isOfflineFallback = false,
                    bannerMessage = banner
                )
            }

            is LotteryResult.Error -> {
                val cached = result.cachedData
                if (!cached.isNullOrEmpty()) {
                    // In-memory offline fallback: show existing data with connection banner
                    val banner = if (result.isCachedDataFixture) {
                        BANNER_OFFLINE_FIXTURE
                    } else {
                        BANNER_OFFLINE_LIVE
                    }

                    _uiState.value = DrawResultsUiState.Success(
                        draws = LotteryUiMapper.mapDraws(cached).toPersistentList(),
                        isRefreshing = false,
                        isFixture = result.isCachedDataFixture,
                        isOfflineFallback = true,
                        bannerMessage = banner
                    )
                } else {
                    // Terminal error with no prior data available
                    val errorMessage = when (result.statusCode) {
                        404 -> ERROR_HTTP_404
                        in 500..599 -> ERROR_SERVER_ERROR
                        else -> ERROR_NO_CONNECTION
                    }

                    // canLoadFixture is ONLY enabled for 404; disabled for 500 and network disconnects
                    val canLoadFixture = (result.statusCode == 404)

                    _uiState.value = DrawResultsUiState.Error(
                        message = errorMessage,
                        statusCode = result.statusCode,
                        canLoadFixture = canLoadFixture
                    )
                }
            }
        }
    }

    companion object {
        const val BANNER_FIXTURE = "Beispieldaten aus der Fallstudie aktiv – keine Echtzeitdaten."
        const val BANNER_OFFLINE_LIVE = "Keine Internetverbindung – vorherige Ziehungsdaten werden angezeigt."
        const val BANNER_OFFLINE_FIXTURE = "Keine Internetverbindung – Beispieldaten werden angezeigt."

        const val ERROR_HTTP_404 = "Die Ziehungsergebnisse konnten nicht geladen werden (HTTP 404). Möchtest du Beispieldaten laden?"
        const val ERROR_SERVER_ERROR = "Der Server ist vorübergehend nicht erreichbar. Bitte versuche es später erneut."
        const val ERROR_NO_CONNECTION = "Keine Verbindung zum Server möglich. Bitte überprüfe deine Internetverbindung."
    }
}
