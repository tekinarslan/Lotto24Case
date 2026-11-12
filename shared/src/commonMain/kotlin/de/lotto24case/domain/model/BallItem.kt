package de.lotto24case.domain.model

/**
 * Representation of a lottery ball with its number value and category.
 *
 * @property value Drawn number value
 * @property type Specific ball category ([BallType.REGULAR], [BallType.SUPER_ZAHL], [BallType.EURO_ZAHL])
 */
data class BallItem(
    val value: Int,
    val type: BallType
)
