package weather.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import weather.data.remote.WeatherClient
import weather.data.repository.WeatherRepositoryImpl
import weather.domain.repository.WeatherRepository

val weatherModule = module {
    singleOf(::WeatherClient)
    singleOf(::WeatherRepositoryImpl).bind<WeatherRepository>()
}