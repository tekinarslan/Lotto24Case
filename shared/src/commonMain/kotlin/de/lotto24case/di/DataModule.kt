package de.lotto24case.di

import de.lotto24case.data.repository.LotteryRepositoryImpl
import de.lotto24case.domain.repository.LotteryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

val dataModule = module {
    single<LotteryRepository> {
        LotteryRepositoryImpl(
            apiService = get(),
            ioDispatcher = Dispatchers.IO
        )
    }
}
