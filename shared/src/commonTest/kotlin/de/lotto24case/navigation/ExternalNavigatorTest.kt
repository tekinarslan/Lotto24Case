package de.lotto24case.navigation

import androidx.compose.ui.platform.UriHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExternalNavigatorTest {

    private class FakeUriHandler : UriHandler {
        val openedUris = mutableListOf<String>()
        var throwOnUriPattern: Regex? = null
        var throwException: Exception = IllegalStateException("Mock URI error")

        override fun openUri(uri: String) {
            val pattern = throwOnUriPattern
            if (pattern != null && pattern.containsMatchIn(uri)) {
                throw throwException
            }
            openedUris.add(uri)
        }
    }

    @Test
    fun `resolveLotteryUrl correctly maps 6aus49 variations to official lotto24 6aus49 URL`() {
        val fakeUriHandler = FakeUriHandler()
        val navigator = ComposeExternalNavigator(fakeUriHandler)

        assertEquals(ExternalNavigator.LOTTO24_LOTTO6AUS49_URL, navigator.resolveLotteryUrl("6aus49"))
        assertEquals(ExternalNavigator.LOTTO24_LOTTO6AUS49_URL, navigator.resolveLotteryUrl("6AUS49"))
        assertEquals(ExternalNavigator.LOTTO24_LOTTO6AUS49_URL, navigator.resolveLotteryUrl("lotto"))
        assertEquals(ExternalNavigator.LOTTO24_LOTTO6AUS49_URL, navigator.resolveLotteryUrl("lotto_6aus49"))
    }

    @Test
    fun `resolveLotteryUrl correctly maps eurojackpot variations to official lotto24 eurojackpot URL`() {
        val fakeUriHandler = FakeUriHandler()
        val navigator = ComposeExternalNavigator(fakeUriHandler)

        assertEquals(ExternalNavigator.LOTTO24_EUROJACKPOT_URL, navigator.resolveLotteryUrl("eurojackpot"))
        assertEquals(ExternalNavigator.LOTTO24_EUROJACKPOT_URL, navigator.resolveLotteryUrl("EUROJACKPOT"))
        assertEquals(ExternalNavigator.LOTTO24_EUROJACKPOT_URL, navigator.resolveLotteryUrl("euro_jackpot"))
    }

    @Test
    fun `resolveLotteryUrl falls back to main lotto24 base URL for unknown lottery`() {
        val fakeUriHandler = FakeUriHandler()
        val navigator = ComposeExternalNavigator(fakeUriHandler)

        assertEquals(ExternalNavigator.LOTTO24_BASE_URL, navigator.resolveLotteryUrl("keno"))
        assertEquals(ExternalNavigator.LOTTO24_BASE_URL, navigator.resolveLotteryUrl("gluecksspirale"))
        assertEquals(ExternalNavigator.LOTTO24_BASE_URL, navigator.resolveLotteryUrl(""))
    }

    @Test
    fun `openLotteryUrl invokes UriHandler with resolved URL`() {
        val fakeUriHandler = FakeUriHandler()
        val navigator = ComposeExternalNavigator(fakeUriHandler)

        navigator.openLotteryUrl("6aus49")
        assertEquals(listOf(ExternalNavigator.LOTTO24_LOTTO6AUS49_URL), fakeUriHandler.openedUris)

        navigator.openLotteryUrl("eurojackpot")
        assertEquals(
            listOf(ExternalNavigator.LOTTO24_LOTTO6AUS49_URL, ExternalNavigator.LOTTO24_EUROJACKPOT_URL),
            fakeUriHandler.openedUris
        )
    }

    @Test
    fun `openLotto24Website opens main lotto24 home page`() {
        val fakeUriHandler = FakeUriHandler()
        val navigator = ComposeExternalNavigator(fakeUriHandler)

        navigator.openLotto24Website()
        assertEquals(listOf(ExternalNavigator.LOTTO24_BASE_URL), fakeUriHandler.openedUris)
    }

    @Test
    fun `openStore on Android targets Google Play market protocol and falls back to web`() {
        // Normal Android store launch
        val successHandler = FakeUriHandler()
        val androidNavigator = ComposeExternalNavigator(successHandler, isIos = false)
        androidNavigator.openStore("de.lotto24")
        assertEquals(listOf("market://details?id=de.lotto24"), successHandler.openedUris)

        // Fallback when market protocol is unavailable
        val fallbackHandler = FakeUriHandler().apply {
            throwOnUriPattern = Regex("^market://")
        }
        val androidFallbackNavigator = ComposeExternalNavigator(fallbackHandler, isIos = false)
        androidFallbackNavigator.openStore("de.lotto24")
        assertEquals(
            listOf("https://play.google.com/store/apps/details?id=de.lotto24"),
            fallbackHandler.openedUris
        )
    }

    @Test
    fun `openStore on iOS targets Apple App Store itms-apps protocol and falls back to web`() {
        // Normal iOS store launch
        val successHandler = FakeUriHandler()
        val iosNavigator = ComposeExternalNavigator(successHandler, isIos = true)
        iosNavigator.openStore(iosAppId = "630925501")
        assertEquals(listOf("itms-apps://apps.apple.com/app/id630925501"), successHandler.openedUris)

        // Fallback when itms-apps protocol is unavailable
        val fallbackHandler = FakeUriHandler().apply {
            throwOnUriPattern = Regex("^itms-apps://")
        }
        val iosFallbackNavigator = ComposeExternalNavigator(fallbackHandler, isIos = true)
        iosFallbackNavigator.openStore(iosAppId = "630925501")
        assertEquals(
            listOf("https://apps.apple.com/app/id630925501"),
            fallbackHandler.openedUris
        )
    }

    @Test
    fun `openUri suppresses platform exceptions safely without crashing the app`() {
        val fakeUriHandler = FakeUriHandler().apply {
            throwOnUriPattern = Regex(".*")
        }
        val navigator = ComposeExternalNavigator(fakeUriHandler)

        // Must not throw exception
        navigator.openUri("invalid://url")
        assertTrue(fakeUriHandler.openedUris.isEmpty())
    }
}
