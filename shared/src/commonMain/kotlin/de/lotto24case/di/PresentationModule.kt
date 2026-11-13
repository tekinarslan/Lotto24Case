package de.lotto24case.di

import de.lotto24case.presentation.viewmodel.DrawResultsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::DrawResultsViewModel)
}
