package de.lotto24case.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

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
 * Uses Koin's internal [GlobalContext.getOrNull] to ensure idempotency.
 * If Koin is already running, this is a no-op; if Koin was stopped (e.g. in test suites
 * via `stopKoin()`), subsequent calls will safely restart it without relying on a stale
 * global boolean flag.
 */
fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    if (GlobalContext.getOrNull() != null) return
    startKoin {
        appDeclaration?.invoke(this)
        modules(appModules)
    }
}
