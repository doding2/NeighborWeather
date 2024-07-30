package weather.data.repository

import co.touchlab.kermit.Logger
import core.util.CommonError
import core.util.Error
import core.util.Result
import core.util.getDataOrNull
import core.util.map
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import weather.data.mapper.toWeather
import weather.data.remote.WeatherClient
import weather.data.util.WeatherWeightCalculator
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherClient: WeatherClient,
    private val weightCalculator: WeatherWeightCalculator
): WeatherRepository {

    private val logger by lazy { Logger.withTag("WeatherRepositoryImpl") }

    override suspend fun getWeather(
        latitude: Double, longitude: Double,
        locationName: String,
        targetToWeight: Map<Neighbor, Double>
    ): Result<Weather, Error> {
        return withContext(Dispatchers.IO) {
            val neighbors = listOf(
                Neighbor.Korea,
                Neighbor.Japan,
                Neighbor.China,
                Neighbor.USA,
                Neighbor.ALL
            )
            val neighborResults = neighbors.map { neighbor ->
                async {
                    try {
                        if (neighbor == Neighbor.Korea) {
                            weatherClient.getKoreaWeather(
                                latitude = latitude,
                                longitude = longitude,
                                locationName = locationName
                            ).map {
                                it.toWeather()
                            }
                        } else {
                            weatherClient.getNeighborWeather(
                                latitude = latitude,
                                longitude = longitude,
                                neighbor = neighbor
                            ).map {
                                it.toWeather(neighbor)
                            }
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        logger.e(e.stackTraceToString())
                        Result.Error(CommonError.UNKNOWN)
                    }
                }
            }.awaitAll()

            neighborResults.forEach {
                if (it is Result.Error) {
                    return@withContext it
                }
            }

            neighborResults
                .mapNotNull { it.getDataOrNull() }
                .let { weathers ->
                    weightCalculator.calculateWeightedSum(
                        weathers = weathers,
                        targetToWeight = targetToWeight
                    )
                }
        }
    }

}