package weather.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import weather.data.remote.WeatherClient
import weather.data.repository.WeatherRepositoryImpl
import weather.data.util.NaverWeatherParser
import weather.domain.repository.WeatherRepository

val weatherModule = module {
    factoryOf(::WeatherClient)
    factoryOf(::NaverWeatherParser)
    singleOf(::WeatherRepositoryImpl).bind<WeatherRepository>()
}