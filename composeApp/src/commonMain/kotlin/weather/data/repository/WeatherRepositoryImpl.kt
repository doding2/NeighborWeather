package weather.data.repository

import core.util.NetworkError
import core.util.Result
import core.util.map
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
            val result = listOf(Neighbor.Japan, Neighbor.China)
                .map { neighbor ->
                    async {
                        weatherClient.getNeighborWeather(
                            latitude = latitude,
                            longitude = longitude,
                            neighbor = neighbor
                        ).map { it.toWeather(neighbor) }
                    }
                }.awaitAll()

            result[0]
        }
    }

}