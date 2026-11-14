package de.lotto24case.di

import de.lotto24case.domain.usecase.GetLotteryResultsUseCase
import de.lotto24case.domain.usecase.LoadFixtureResultsUseCase
import de.lotto24case.widget.usecase.GetWidgetDrawDataUseCase
import org.koin.dsl.module

val domainModule = module {
    factory {
        GetLotteryResultsUseCase(repository = get())
    }
    factory {
        LoadFixtureResultsUseCase(repository = get())
    }
    factory {
        GetWidgetDrawDataUseCase(repository = get())
    }
}
