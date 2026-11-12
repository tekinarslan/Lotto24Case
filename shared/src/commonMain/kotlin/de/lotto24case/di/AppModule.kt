package de.lotto24case.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Aggregated Koin modules for the shared multiplatform codebase.
 */
val appModules = listOf(
    networkModule,
    dataModule,
    domainModule
)

/**
 * Initializes Koin for Android or multiplatform host environments.
 */
fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    startKoin {
        appDeclaration?.invoke(this)
        modules(appModules)
    }
}
