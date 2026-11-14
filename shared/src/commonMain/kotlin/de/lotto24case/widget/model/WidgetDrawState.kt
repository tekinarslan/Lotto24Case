package de.lotto24case.widget.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Lightweight representation of a supplementary lottery game (e.g. Spiel 77, SUPER6)
 * for home screen widgets.
 */
@Immutable
data class WidgetSupplementaryGame(
    val name: String,
    val winningNumber: String
)

/**
 * Lightweight cross-platform presentation state model tailored for home screen widgets
 * (Android Jetpack Glance & iOS WidgetKit).
 *
 * Annotated with [@Immutable] and uses [ImmutableList] to guarantee compile-time stability
 * in Compose runtimes (enabling skippable recompositions and zero runtime stability overhead).
 */
@Immutable
data class WidgetDrawState(
    val lotteryId: String,
    val title: String,
    val formattedDate: String,
    val nextDrawText: String?,
    val jackpotAmountValue: String?,
    val jackpotUnitValue: String?,
    val jackpotFormatted: String?,
    val mainBallsLabel: String,
    val specialBallLabel: String?,
    val regularNumbers: ImmutableList<Int>,
    val specialNumbers: ImmutableList<Int>,
    val supplementaryGames: ImmutableList<WidgetSupplementaryGame> = persistentListOf(),
    val anchorLabel: String,
    val isFixture: Boolean = false,
    val specialNumberLabel: String = specialBallLabel ?: ""
)
