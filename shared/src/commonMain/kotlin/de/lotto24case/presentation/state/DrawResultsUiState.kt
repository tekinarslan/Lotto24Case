package de.lotto24case.presentation.state

import androidx.compose.runtime.Immutable
import de.lotto24case.presentation.model.LotteryCardUiModel
import kotlinx.collections.immutable.ImmutableList

/**
 * Unidirectional UI state for the Draw Results screen.
 */
@Immutable
sealed interface DrawResultsUiState {

    /**
     * Initial screen loading state.
     */
    @Immutable
    data object Loading : DrawResultsUiState

    /**
     * Successfully displaying lottery draw cards.
     *
     * @property draws The immutable list of cards to render.
     * @property isRefreshing True during active pull-to-refresh swipe.
     * @property isFixture True when displaying sample PDF fixture data.
     * @property isOfflineFallback True when network failed during refresh and previous cached draws are shown.
     * @property bannerMessage Optional message string for informational banner (in German).
     */
    @Immutable
    data class Success(
        val draws: ImmutableList<LotteryCardUiModel>,
        val isRefreshing: Boolean = false,
        val isFixture: Boolean = false,
        val isOfflineFallback: Boolean = false,
        val bannerMessage: String? = null
    ) : DrawResultsUiState

    /**
     * Terminal error state when no data is available to display.
     *
     * @property message Localized German error description.
     * @property statusCode Optional HTTP response code (e.g. 404).
     * @property canLoadFixture Whether the "Beispieldaten laden" action button should be presented.
     */
    @Immutable
    data class Error(
        val message: String,
        val statusCode: Int? = null,
        val canLoadFixture: Boolean = true
    ) : DrawResultsUiState
}
