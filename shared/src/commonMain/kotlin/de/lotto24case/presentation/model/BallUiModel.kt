package de.lotto24case.presentation.model

import androidx.compose.runtime.Immutable

/**
 * Immutable UI model for a single lottery ball item.
 *
 * Distinct styling will be applied in Compose based on [type]:
 * - [BallUiType.REGULAR]: Classic white background with dark border & text.
 * - [BallUiType.SUPER_ZAHL]: Distinct red tint/border for LOTTO 6aus49 Superzahl.
 * - [BallUiType.EURO_ZAHL]: Distinct warm amber/gold tint/border for Eurojackpot Eurozahlen.
 *
 * @property number The drawn ball number (e.g. 4, 19, 7).
 * @property type The ball UI classification.
 * @property index Sequence index within the draw tray (used for deterministic stagger animation delay).
 */
@Immutable
data class BallUiModel(
    val number: Int,
    val type: BallUiType,
    val index: Int = 0
)
