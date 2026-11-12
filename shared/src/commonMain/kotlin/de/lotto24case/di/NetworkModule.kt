package de.lotto24case.di

import de.lotto24case.data.remote.api.KtorClientFactory
import de.lotto24case.data.remote.api.Lotto24ApiService
import de.lotto24case.data.remote.api.Lotto24ApiServiceImpl
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        KtorClientFactory.create()
    }
    single<Lotto24ApiService> {
        Lotto24ApiServiceImpl(httpClient = get())
    }
}
