package weather.data.repository

import core.util.CommonError
import core.util.Error
import core.util.Result
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
    private val weatherClient: WeatherClient
): WeatherRepository {

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double
    ): Result<Weather, Error> {
        return withContext(Dispatchers.IO) {
            val neighbors = listOf(
                Neighbor.Japan,
                Neighbor.China,
                Neighbor.USA
            )
            val neighborResults = neighbors.map { neighbor ->
                async {
                    try {
//                        if (neighbor == Neighbor.Korea) {
//                            getKoreaWeather(latitude, longitude)
//                        } else {
//                            weatherClient.getNeighborWeather(latitude, longitude, neighbor).map {
//                                it.toWeather(neighbor)
//                            }
//                        }
                        weatherClient.getNeighborWeather(latitude, longitude, neighbor).map {
                            it.toWeather(neighbor)
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        Result.Error(CommonError.UNKNOWN)
                    }
                }
            }.awaitAll()

            // TODO: combine forecasts into one with weight

            neighborResults[0]
        }
    }

//    private suspend fun getKoreaWeather(latitude: Double, longitude: Double): Result<Weather, Error> {
//        return withContext(Dispatchers.IO) {
//            val currentDeferred = async {
//                weatherClient.getKoreaCurrentWeather(latitude, longitude)
//            }
//
//            val current = when (val currentResult = currentDeferred.await()) {
//                is Result.Success -> {
//                    currentResult.data.toCurrentWeather()
//                        ?: return@withContext Result.Error(CommonError.NULL_POINTER)
//                }
//                is Result.Error -> {
//                    return@withContext Result.Error(currentResult.error)
//                }
//            }
//
//
//            Result.Success(
//                Weather(
//                    latitude = latitude,
//                    longitude = longitude,
//                    neighbor = Neighbor.Korea,
//                    current = current,
//                    hourly = ,
//                    daily = ,
//                )
//            )
//        }
//    }

}