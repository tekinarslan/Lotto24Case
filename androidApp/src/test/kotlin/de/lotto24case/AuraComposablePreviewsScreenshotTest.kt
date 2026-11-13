package de.lotto24case

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * JVM-based screenshot test runner utilizing Roborazzi and ComposablePreviewScanner.
 *
 * Capabilities:
 * - Automatically scans `@Preview` functions across `de.lotto24case.presentation.ui.preview`.
 * - Parameterized execution via Robolectric (running headless on the JVM without an emulator).
 * - Tests dark & light mode, 3D lottery balls, 6aus49, Eurojackpot, Loading, Success, and Error screens.
 * - Hardware graphics acceleration mode enabled for high-fidelity rendering.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AuraComposablePreviewsScreenshotTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>
) {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun snapshotPreview() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview)
            .ignoreClassName()
            .build()

        val filePath = "screenshots/$screenshotId.png"

        composeTestRule.setContent {
            preview()
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = filePath,
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(
                    changeThreshold = 0.01f
                )
            )
        )
    }

    companion object {
        private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .scanPackageTrees("de.lotto24case.presentation.ui.preview")
                .includePrivatePreviews()
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
    }
}
