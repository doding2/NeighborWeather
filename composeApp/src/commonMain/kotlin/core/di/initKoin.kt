package core.di

import home.di.homeModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import weather.di.weatherModule

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(networkModule, weatherModule, homeModule)
    }
}