package de.lotto24case.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.lotto24case.presentation.model.LotteryCardUiModel
import de.lotto24case.presentation.state.DrawResultsUiState
import de.lotto24case.presentation.theme.AuraSpacing
import de.lotto24case.presentation.theme.AuraTheme
import de.lotto24case.presentation.theme.AuraTypography
import de.lotto24case.presentation.ui.component.LotteryCard
import de.lotto24case.presentation.viewmodel.DrawResultsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Top-level route container that resolves the [DrawResultsViewModel] and lifecycle-aware state.
 *
 * Architecture & Stability:
 * - Does NOT expose [DrawResultsViewModel] as a composable parameter, preventing unstable parameter
 *   warnings on the route itself.
 * - Resolves ViewModel internally via Koin and passes only immutable UI state and callbacks to [DrawResultsScreen].
 * - [DrawResultsScreen] remains 100% stateless, skippable, and easy to test or preview.
 */
@Composable
fun DrawResultsRoute(
    modifier: Modifier = Modifier,
    onCardClick: ((LotteryCardUiModel) -> Unit)? = null
) {
    val viewModel: DrawResultsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    val defaultCardClickHandler: (LotteryCardUiModel) -> Unit = onCardClick ?: { card ->
        val url = when (card.id.lowercase()) {
            "6aus49" -> "https://www.lotto24.de/lotto-6aus49/zahlen-quoten/"
            "eurojackpot" -> "https://www.lotto24.de/eurojackpot/zahlen-quoten/"
            else -> "https://www.lotto24.de/"
        }
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
            // Safe fallback if browser intent is not resolved
        }
    }

    DrawResultsScreen(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetryClicked,
        onLoadFixture = viewModel::onLoadFixtureClicked,
        onCardClick = defaultCardClickHandler,
        modifier = modifier
    )
}

/**
 * Pure stateless and skippable screen displaying draw results.
 *
 * Conforms to Compose stability best practices:
 * - No ViewModel reference in parameters.
 * - Accepts only [DrawResultsUiState] and function callbacks.
 */
@Composable
fun DrawResultsScreen(
    uiState: DrawResultsUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadFixture: () -> Unit,
    onCardClick: (LotteryCardUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    DrawResultsContent(
        uiState = uiState,
        onRefresh = onRefresh,
        onRetry = onRetry,
        onLoadFixture = onLoadFixture,
        onCardClick = onCardClick,
        modifier = modifier
    )
}

/**
 * Stateless content matching Aura Lotto Design specifications:
 * - Clean white top bar with "img Aura Lotto" branding.
 * - Luminous background (#F8FAFC).
 * - Pull-to-refresh on both success and error states.
 * - Colors resolved semantically via [AuraTheme.colors].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawResultsContent(
    uiState: DrawResultsUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadFixture: () -> Unit,
    onCardClick: (LotteryCardUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "img" Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.surfaceContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "img",
                                color = colors.onSurfaceSubtle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // "Aura Lotto" Title
                        Text(
                            text = "Aura Lotto",
                            style = AuraTypography.headlineSm.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.primary
                ),
                modifier = Modifier.shadow(2.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is DrawResultsUiState.Loading -> {
                    LoadingView()
                }

                is DrawResultsUiState.Error -> {
                    val pullRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = onRefresh,
                        state = pullRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ErrorView(
                            state = uiState,
                            onRetry = onRetry,
                            onLoadFixture = onLoadFixture
                        )
                    }
                }

                is DrawResultsUiState.Success -> {
                    val pullRefreshState = rememberPullToRefreshState()

                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        state = pullRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 32.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (uiState.bannerMessage != null) {
                                item(key = "banner") {
                                    InfoBanner(
                                        message = uiState.bannerMessage,
                                        isOffline = uiState.isOfflineFallback
                                    )
                                }
                            }

                            items(
                                items = uiState.draws,
                                key = { it.id }
                            ) { cardModel ->
                                LotteryCard(
                                    model = cardModel,
                                    onClick = { onCardClick(cardModel) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    val colors = AuraTheme.colors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AuraSpacing.spaceMd)
        ) {
            CircularProgressIndicator(
                color = colors.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Ziehungsergebnisse werden geladen...",
                style = AuraTypography.bodyMd,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoBanner(
    message: String,
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    val bannerBg = if (isOffline) colors.offlineBannerBackground else colors.infoBannerBackground
    val bannerBorder = if (isOffline) colors.offlineBannerBorder else colors.infoBannerBorder
    val bannerText = if (isOffline) colors.offlineBannerText else colors.infoBannerText
    val icon = if (isOffline) "⚡" else "ℹ️"
    val bannerShape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(bannerShape)
            .background(bannerBg)
            .border(1.dp, bannerBorder, bannerShape)
            .padding(horizontal = AuraSpacing.spaceMd, vertical = AuraSpacing.spaceSm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AuraSpacing.spaceSm)
        ) {
            Text(
                text = icon,
                fontSize = 14.sp
            )
            Text(
                text = message,
                style = AuraTypography.bodySm.copy(fontWeight = FontWeight.SemiBold),
                color = bannerText
            )
        }
    }
}

@Composable
private fun ErrorView(
    state: DrawResultsUiState.Error,
    onRetry: () -> Unit,
    onLoadFixture: () -> Unit
) {
    val colors = AuraTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AuraSpacing.spaceLg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AuraSpacing.spaceMd),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠️",
                fontSize = 44.sp
            )

            Text(
                text = "Ergebnisse konnten nicht geladen werden",
                style = AuraTypography.headlineSm.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = state.message,
                style = AuraTypography.bodyMd,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(AuraSpacing.spaceSm))

            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AuraSpacing.spaceSm)
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text(
                        text = "Erneut versuchen",
                        maxLines = 1
                    )
                }

                if (state.canLoadFixture) {
                    OutlinedButton(
                        onClick = onLoadFixture,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.primary
                        )
                    ) {
                        Text(
                            text = "Beispieldaten laden",
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
