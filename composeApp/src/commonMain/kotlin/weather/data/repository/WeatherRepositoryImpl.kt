package weather.data.repository

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
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherClient: WeatherClient,
): WeatherRepository {

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        locationName: String
    ): Result<Weather, Error> {
        return withContext(Dispatchers.IO) {
            val neighbors = listOf(
                Neighbor.Korea,
                Neighbor.Japan,
                Neighbor.China,
                Neighbor.USA
            )
            val neighborResults = neighbors.map { neighbor ->
                async {
                    try {
                        if (neighbor == Neighbor.Korea) {
                            weatherClient.getKoreaWeather(latitude, longitude, locationName).map {
                                it.toWeather()
                            }
                        } else {
                            weatherClient.getNeighborWeather(latitude, longitude, neighbor).map {
                                it.toWeather(neighbor)
                            }
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        Result.Error(CommonError.UNKNOWN)
                    }
                }
            }.awaitAll()

            // fill missing precipitation probability data in japan, china
            val filledNeighborResults = neighborResults.toMutableList()
            val usaWeather = neighborResults.find {
                it.getDataOrNull()?.let { weather ->
                    weather.neighbor == Neighbor.USA
                } == true
            }?.getDataOrNull()

            usaWeather?.let { usa ->
                neighborResults.forEachIndexed { index, result ->
                    result.getDataOrNull()?.let { weather ->
                        val isNoPrecipitationProbability =
                            weather.neighbor != Neighbor.Korea && weather.neighbor != Neighbor.USA
                        if (isNoPrecipitationProbability) {
                            weather.copy(
                                current = weather.current.copy(
                                    precipitationProbability = usa.current.precipitationProbability
                                ),
                                hourly = weather.hourly.copy(
                                    precipitationProbability = usa.hourly.precipitationProbability
                                ),
                                daily = weather.daily.copy(
                                    precipitationProbability = usa.daily.precipitationProbability
                                )
                            ).also {
                                filledNeighborResults[index] = Result.Success(it)
                            }
                        }
                    }
                }
            }

            // TODO: combine forecasts into one with weight

            filledNeighborResults[0]
        }
    }

}