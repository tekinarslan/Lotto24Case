package de.lotto24case.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler

/**
 * Interface abstraction for handling external navigation (Web URLs, App Store / Play Store, etc.)
 * across Multiplatform targets. Marked with [@Stable] for Compose compiler stability.
 */
@Stable
interface ExternalNavigator {

    /**
     * Resolves the target external URL for a given lottery identifier.
     */
    fun resolveLotteryUrl(lotteryId: String): String

    /**
     * Opens the official results/play website for the given lottery identifier.
     */
    fun openLotteryUrl(lotteryId: String)

    /**
     * Opens the main Lotto24 website.
     */
    fun openLotto24Website()

    /**
     * Resolves primary store URI (market:// or itms-apps://) and web fallback URL for current platform.
     */
    fun resolveStoreUrls(
        androidPackage: String = LOTTO24_ANDROID_PACKAGE,
        iosAppId: String = LOTTO24_IOS_APP_ID
    ): Pair<String, String>

    /**
     * Attempts to open the platform-specific store app page (Google Play on Android, App Store on iOS),
     * falling back to the corresponding web URL if native store URI fails.
     */
    fun openStore(
        androidPackage: String = LOTTO24_ANDROID_PACKAGE,
        iosAppId: String = LOTTO24_IOS_APP_ID
    )

    /**
     * Safely opens an arbitrary URI/URL without crashing if the platform handler fails.
     */
    fun openUri(uriString: String)

    companion object {
        const val LOTTO24_BASE_URL = "https://www.lotto24.de"
        const val LOTTO24_LOTTO6AUS49_URL = "https://www.lotto24.de/lotto-6aus49/zahlen-quoten/"
        const val LOTTO24_EUROJACKPOT_URL = "https://www.lotto24.de/eurojackpot/zahlen-quoten/"
        const val LOTTO24_ANDROID_PACKAGE = "de.lotto24"
        const val LOTTO24_IOS_APP_ID = "630925501"
    }
}

/**
 * Default implementation of [ExternalNavigator] backed by Compose's [UriHandler].
 */
@Stable
class ComposeExternalNavigator(
    private val uriHandler: UriHandler,
    private val isIos: Boolean = isIosPlatform
) : ExternalNavigator {

    override fun resolveLotteryUrl(lotteryId: String): String {
        return when (lotteryId.lowercase().trim()) {
            "6aus49", "lotto", "lotto_6aus49", "lotto6aus49" -> ExternalNavigator.LOTTO24_LOTTO6AUS49_URL
            "eurojackpot", "euro_jackpot" -> ExternalNavigator.LOTTO24_EUROJACKPOT_URL
            else -> ExternalNavigator.LOTTO24_BASE_URL
        }
    }

    override fun openLotteryUrl(lotteryId: String) {
        openUri(resolveLotteryUrl(lotteryId))
    }

    override fun openLotto24Website() {
        openUri(ExternalNavigator.LOTTO24_BASE_URL)
    }

    override fun resolveStoreUrls(androidPackage: String, iosAppId: String): Pair<String, String> {
        return if (isIos) {
            val nativeUri = "itms-apps://apps.apple.com/app/id$iosAppId"
            val webFallback = "https://apps.apple.com/app/id$iosAppId"
            nativeUri to webFallback
        } else {
            val nativeUri = "market://details?id=$androidPackage"
            val webFallback = "https://play.google.com/store/apps/details?id=$androidPackage"
            nativeUri to webFallback
        }
    }

    override fun openStore(androidPackage: String, iosAppId: String) {
        val (nativeUri, webFallback) = resolveStoreUrls(androidPackage, iosAppId)
        try {
            uriHandler.openUri(nativeUri)
        } catch (_: Exception) {
            // Native store protocol failed (e.g. desktop simulator or no store app installed), use web fallback
            try {
                uriHandler.openUri(webFallback)
            } catch (_: Exception) {
                // Safe ignore if browser handler cannot resolve
            }
        }
    }

    override fun openUri(uriString: String) {
        try {
            uriHandler.openUri(uriString)
        } catch (_: Exception) {
            // Safe fallback if browser/intent handler is not resolved on host device
        }
    }
}

/**
 * Helper to remember an [ExternalNavigator] instance in Composable scope.
 */
@Composable
fun rememberExternalNavigator(
    uriHandler: UriHandler = LocalUriHandler.current
): ExternalNavigator {
    return remember(uriHandler) {
        ComposeExternalNavigator(uriHandler)
    }
}

/**
 * CompositionLocal providing access to [ExternalNavigator].
 */
val LocalExternalNavigator = staticCompositionLocalOf<ExternalNavigator> {
    object : ExternalNavigator {
        override fun resolveLotteryUrl(lotteryId: String): String = ExternalNavigator.LOTTO24_BASE_URL
        override fun openLotteryUrl(lotteryId: String) {}
        override fun openLotto24Website() {}
        override fun resolveStoreUrls(androidPackage: String, iosAppId: String): Pair<String, String> =
            "" to ""
        override fun openStore(androidPackage: String, iosAppId: String) {}
        override fun openUri(uriString: String) {}
    }
}
