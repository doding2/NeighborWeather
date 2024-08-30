package home.di

import home.presentation.HomeViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::HomeViewModel)
}