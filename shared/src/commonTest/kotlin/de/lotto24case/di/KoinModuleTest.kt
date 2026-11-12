package de.lotto24case.di

import de.lotto24case.data.remote.api.Lotto24ApiService
import de.lotto24case.domain.repository.LotteryRepository
import de.lotto24case.domain.usecase.GetLotteryResultsUseCase
import de.lotto24case.domain.usecase.LoadFixtureResultsUseCase
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class KoinModuleTest : KoinTest {

    @BeforeTest
    fun setUp() {
        stopKoin()
        startKoin {
            modules(appModules)
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verifies koin dependency graph resolves all singletons and use cases`() {
        assertNotNull(get<HttpClient>())
        assertNotNull(get<Lotto24ApiService>())
        assertNotNull(get<LotteryRepository>())
        assertNotNull(get<GetLotteryResultsUseCase>())
        assertNotNull(get<LoadFixtureResultsUseCase>())
    }
}
