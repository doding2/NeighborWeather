package core.di

import home.di.homeModule
import map.di.mapModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import weather.di.weatherDatabaseModule
import weather.di.weatherModule

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(coreModule, networkModule, weatherModule, weatherDatabaseModule, homeModule, mapModule)
    }
}