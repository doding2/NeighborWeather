package map.di

import map.presentation.MapViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val mapModule = module {
    viewModelOf(::MapViewModel)
}