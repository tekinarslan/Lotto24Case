package de.lotto24case.presentation.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.lotto24case.domain.model.LotteryType
import de.lotto24case.presentation.model.BallUiModel
import de.lotto24case.presentation.model.BallUiType
import de.lotto24case.presentation.model.LotteryCardUiModel
import de.lotto24case.presentation.model.SupplementaryGameUiModel
import de.lotto24case.presentation.theme.AuraDarkColorScheme
import de.lotto24case.presentation.theme.AuraLottoTheme
import de.lotto24case.presentation.ui.component.LotteryBall
import de.lotto24case.presentation.ui.component.LotteryCard
import kotlinx.collections.immutable.persistentListOf

val SampleLotto6aus49Card = LotteryCardUiModel(
    id = "6aus49",
    lotteryType = LotteryType.LOTTO_6AUS49,
    title = "LOTTO 6aus49",
    formattedDrawDate = "Fr., 18.09.2020",
    nextDrawBadgeText = "Nächste Ziehung: Mi., 23.09.2020",
    remainingTimeText = "3 Tage 23 Std.",
    jackpotFormatted = "6 MIO €",
    jackpotAmountValue = "23",
    jackpotUnitValue = "MIO",
    mainBallsLabel = "GEWINNZAHLEN",
    specialBallLabel = "Superzahl: 7",
    superNumber = 7,
    balls = persistentListOf(
        BallUiModel(4, BallUiType.REGULAR, index = 0),
        BallUiModel(19, BallUiType.REGULAR, index = 1),
        BallUiModel(24, BallUiType.REGULAR, index = 2),
        BallUiModel(43, BallUiType.REGULAR, index = 3),
        BallUiModel(44, BallUiType.REGULAR, index = 4),
        BallUiModel(45, BallUiType.REGULAR, index = 5),
        BallUiModel(7, BallUiType.SUPER_ZAHL, index = 6)
    ),
    regularBalls = persistentListOf(
        BallUiModel(4, BallUiType.REGULAR, index = 0),
        BallUiModel(19, BallUiType.REGULAR, index = 1),
        BallUiModel(24, BallUiType.REGULAR, index = 2),
        BallUiModel(43, BallUiType.REGULAR, index = 3),
        BallUiModel(44, BallUiType.REGULAR, index = 4),
        BallUiModel(45, BallUiType.REGULAR, index = 5)
    ),
    specialBalls = persistentListOf(
        BallUiModel(7, BallUiType.SUPER_ZAHL, index = 6)
    ),
    supplementaryGames = persistentListOf(
        SupplementaryGameUiModel("Spiel 77", "7312244"),
        SupplementaryGameUiModel("SUPER6", "112430")
    ),
    hasJackpot = true
)

val SampleEurojackpotCard = LotteryCardUiModel(
    id = "eurojackpot",
    lotteryType = LotteryType.EUROJACKPOT,
    title = "Eurojackpot",
    formattedDrawDate = "Fr., 18.09.2020",
    nextDrawBadgeText = "Nächste Ziehung: Fr., 25.09.2020",
    remainingTimeText = "4 Tage 18 Std.",
    jackpotFormatted = "80 MIO €",
    jackpotAmountValue = "23",
    jackpotUnitValue = "MIO",
    mainBallsLabel = "5 GEWINNZAHLEN",
    specialBallLabel = "2 EUROZAHLEN",
    balls = persistentListOf(
        BallUiModel(1, BallUiType.REGULAR, index = 0),
        BallUiModel(12, BallUiType.REGULAR, index = 1),
        BallUiModel(15, BallUiType.REGULAR, index = 2),
        BallUiModel(31, BallUiType.REGULAR, index = 3),
        BallUiModel(47, BallUiType.REGULAR, index = 4),
        BallUiModel(9, BallUiType.EURO_ZAHL, index = 5),
        BallUiModel(10, BallUiType.EURO_ZAHL, index = 6)
    ),
    regularBalls = persistentListOf(
        BallUiModel(1, BallUiType.REGULAR, index = 0),
        BallUiModel(12, BallUiType.REGULAR, index = 1),
        BallUiModel(15, BallUiType.REGULAR, index = 2),
        BallUiModel(31, BallUiType.REGULAR, index = 3),
        BallUiModel(47, BallUiType.REGULAR, index = 4)
    ),
    specialBalls = persistentListOf(
        BallUiModel(9, BallUiType.EURO_ZAHL, index = 5),
        BallUiModel(10, BallUiType.EURO_ZAHL, index = 6)
    ),
    supplementaryGames = persistentListOf(),
    hasJackpot = true
)

@Preview
@Composable
fun PreviewLotteryBallVariantsLight() {
    AuraLottoTheme(darkTheme = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LotteryBall(BallUiModel(4, BallUiType.REGULAR))
            LotteryBall(BallUiModel(7, BallUiType.SUPER_ZAHL))
            LotteryBall(BallUiModel(10, BallUiType.EURO_ZAHL))
        }
    }
}

@Preview
@Composable
fun PreviewLotteryBallVariantsDark() {
    AuraLottoTheme(darkTheme = true) {
        Row(
            modifier = Modifier
                .background(AuraDarkColorScheme.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LotteryBall(BallUiModel(4, BallUiType.REGULAR))
            LotteryBall(BallUiModel(7, BallUiType.SUPER_ZAHL))
            LotteryBall(BallUiModel(10, BallUiType.EURO_ZAHL))
        }
    }
}

@Preview
@Composable
fun PreviewLotto6aus49CardLight() {
    AuraLottoTheme(darkTheme = false) {
        LotteryCard(
            model = SampleLotto6aus49Card,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun PreviewLotto6aus49CardDark() {
    AuraLottoTheme(darkTheme = true) {
        LotteryCard(
            model = SampleLotto6aus49Card,
            modifier = Modifier
                .background(AuraDarkColorScheme.background)
                .padding(16.dp)
        )
    }
}

@Preview
@Composable
fun PreviewEurojackpotCardLight() {
    AuraLottoTheme(darkTheme = false) {
        LotteryCard(
            model = SampleEurojackpotCard,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun PreviewEurojackpotCardDark() {
    AuraLottoTheme(darkTheme = true) {
        LotteryCard(
            model = SampleEurojackpotCard,
            modifier = Modifier
                .background(AuraDarkColorScheme.background)
                .padding(16.dp)
        )
    }
}

@Preview
@Composable
fun PreviewDrawResultsScreenLoadingLight() {
    AuraLottoTheme(darkTheme = false) {
        de.lotto24case.presentation.ui.screen.DrawResultsScreen(
            uiState = de.lotto24case.presentation.state.DrawResultsUiState.Loading,
            onRefresh = {},
            onRetry = {},
            onLoadFixture = {},
            onCardClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewDrawResultsScreenSuccessLight() {
    AuraLottoTheme(darkTheme = false) {
        de.lotto24case.presentation.ui.screen.DrawResultsScreen(
            uiState = de.lotto24case.presentation.state.DrawResultsUiState.Success(
                draws = persistentListOf(SampleLotto6aus49Card, SampleEurojackpotCard),
                isRefreshing = false,
                isFixture = false
            ),
            onRefresh = {},
            onRetry = {},
            onLoadFixture = {},
            onCardClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewDrawResultsScreenSuccessDark() {
    AuraLottoTheme(darkTheme = true) {
        de.lotto24case.presentation.ui.screen.DrawResultsScreen(
            uiState = de.lotto24case.presentation.state.DrawResultsUiState.Success(
                draws = persistentListOf(SampleLotto6aus49Card, SampleEurojackpotCard),
                isRefreshing = false,
                isFixture = false
            ),
            onRefresh = {},
            onRetry = {},
            onLoadFixture = {},
            onCardClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewDrawResultsScreenErrorLight() {
    AuraLottoTheme(darkTheme = false) {
        de.lotto24case.presentation.ui.screen.DrawResultsScreen(
            uiState = de.lotto24case.presentation.state.DrawResultsUiState.Error(
                message = "Netzwerkfehler: Keine Internetverbindung",
                canLoadFixture = true
            ),
            onRefresh = {},
            onRetry = {},
            onLoadFixture = {},
            onCardClick = {}
        )
    }
}

