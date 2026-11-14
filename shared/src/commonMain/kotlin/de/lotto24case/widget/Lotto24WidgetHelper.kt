package de.lotto24case.widget

import de.lotto24case.di.initKoin
import de.lotto24case.widget.model.WidgetDrawState
import de.lotto24case.widget.usecase.GetWidgetDrawDataUseCase
import org.koin.mp.KoinPlatform

/**
 * Platform-agnostic helper for home screen widgets (Android Jetpack Glance & iOS WidgetKit)
 * to retrieve normalized lottery draw data with automatic Koin dependency resolution and fallback.
 */
object Lotto24WidgetHelper {

    suspend fun getWidgetDrawData(forceRefresh: Boolean = false): List<WidgetDrawState> {
        val koin = KoinPlatform.getKoinOrNull() ?: run {
            initKoin()
            KoinPlatform.getKoin()
        }
        val useCase = koin.get<GetWidgetDrawDataUseCase>()
        return useCase(forceRefresh)
    }
}
