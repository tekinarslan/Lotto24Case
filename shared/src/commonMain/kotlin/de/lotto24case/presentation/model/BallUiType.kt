package de.lotto24case.presentation.model

/**
 * Visual styling classification for lottery balls in the presentation layer.
 *
 * Fully decoupled from domain enums to enforce Clean Architecture boundaries.
 */
enum class BallUiType {
    REGULAR,
    SUPER_ZAHL,
    EURO_ZAHL
}
