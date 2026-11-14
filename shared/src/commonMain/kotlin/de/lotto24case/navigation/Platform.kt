package de.lotto24case.navigation

/**
 * Platform indicator used for target-specific URI scheme resolution
 * (e.g. Google Play on Android vs Apple App Store on iOS).
 */
expect val isIosPlatform: Boolean
