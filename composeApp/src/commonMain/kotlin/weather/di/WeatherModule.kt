package weather.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import weather.data.remote.WeatherClient
import weather.data.repository.WeatherRepositoryImpl
import weather.data.util.KoreaWeatherParser
import weather.data.util.WeatherPreprocessor
import weather.data.util.WeatherWeightCalculator
import weather.domain.repository.WeatherRepository

val weatherModule = module {
    factoryOf(::WeatherClient)
    factoryOf(::KoreaWeatherParser)
    factoryOf(::WeatherPreprocessor)
    factoryOf(::WeatherWeightCalculator)
    singleOf(::WeatherRepositoryImpl).bind<WeatherRepository>()
}