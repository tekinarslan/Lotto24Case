package de.lotto24case.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

/**
 * Aggregated Koin modules for the shared multiplatform codebase.
 */
val appModules = listOf(
    networkModule,
    dataModule,
    domainModule,
    presentationModule
)

/**
 * Initializes Koin for Android, iOS, or multiplatform host environments.
 *
 * Uses multiplatform [KoinPlatform.getKoinOrNull] to ensure idempotency.
 * If Koin is already running, this is a no-op; if Koin was stopped (e.g. in test suites
 * via `stopKoin()`), subsequent calls will safely restart it.
 */
fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin {
        appDeclaration?.invoke(this)
        modules(appModules)
    }
}
