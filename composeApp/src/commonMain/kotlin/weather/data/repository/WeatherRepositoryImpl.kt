package weather.data.repository

import core.util.NetworkError
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
    ): Result<Weather, NetworkError> {
        return withContext(Dispatchers.IO) {
            val neighbors = listOf(Neighbor.Japan, Neighbor.China)
            val result = neighbors.map { neighbor ->
                async {
                    try {
                        weatherClient.getNeighborWeather(
                            latitude = latitude,
                            longitude = longitude,
                            neighbor = neighbor
                        ).map { it.toWeather() }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                        Result.Error(NetworkError.UNKNOWN)
                    }
                }
            }.awaitAll()

            result[0]
        }
    }

}