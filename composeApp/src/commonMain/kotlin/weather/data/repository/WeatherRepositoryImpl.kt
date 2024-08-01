package weather.data.repository

import core.util.Error
import core.util.Result
import core.util.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import weather.data.mapper.toWeather
import weather.data.remote.WeatherClient
import weather.data.util.KoreaWeatherParser.KoreaWeatherParserException
import weather.data.util.WeatherWeightCalculator
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherClient: WeatherClient,
    private val weightCalculator: WeatherWeightCalculator
): WeatherRepository {

    override suspend fun getWeather(
        latitude: Double, longitude: Double,
        locationName: String,
        targetToWeight: Map<Neighbor, Double>
    ): Result<Weather, Error> {
        return withContext(Dispatchers.IO) {
            val targetNeighbors = setOf(*targetToWeight.keys.toTypedArray(), Neighbor.ALL)
            val weatherResults = targetNeighbors.map { neighbor ->
                async {
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
                }
            }.awaitAll()

            // MalformedInputException is occurred in
            // iOS internal charset encoding process randomly.
            // We can't expect it, so this error is ignored until bug is fixed.
            // Otherwise, unwrap the result data or return the error.
            val weathers = weatherResults.mapNotNull {
                when (it) {
                    is Result.Success -> it.data
                    is Result.Error -> {
                        if (it.error == KoreaWeatherParserException.MALFORMED_INPUT_EXCEPTION) {
                            null
                        } else {
                            return@withContext it
                        }
                    }
                }
            }
            val successNeighbors = weathers.map { it.neighbor }
            val successTargetToWeight = targetToWeight.filterKeys { it in successNeighbors }

            return@withContext weightCalculator.calculateWeightedSum(
                weathers = weathers,
                targetToWeight = successTargetToWeight
            )
        }
    }

}