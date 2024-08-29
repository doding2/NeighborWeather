package weather.data.repository

import co.touchlab.kermit.Logger
import core.util.Error
import core.util.Result
import core.util.map
import core.util.unzip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import weather.data.local.WeatherDatabase
import weather.data.mapper.toWeather
import weather.data.mapper.toWeatherEntity
import weather.data.remote.WeatherClient
import weather.data.util.KoreaWeatherParser.KoreaWeatherParserException
import weather.data.util.WeatherWeightCalculator
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherDatabase: WeatherDatabase,
    private val weatherClient: WeatherClient,
    private val weightCalculator: WeatherWeightCalculator
): WeatherRepository {
    private val logger by lazy { Logger.withTag("WeatherRepositoryImpl") }

    override suspend fun getWeathers(
        latitude: Double, longitude: Double,
        locationName: String,
        targetToWeight: Map<Neighbor, Double>
    ): Flow<Result<Weather, Error>> {
        return withContext(Dispatchers.IO) {
            channelFlow {
                val targetNeighbors = setOf(*targetToWeight.keys.toTypedArray(), Neighbor.ALL)

                // load from Remote API
                launch {
                    val remoteWeatherResults = getRemoteWeathers(
                        latitude = latitude,
                        longitude = longitude,
                        locationName = locationName,
                        targetNeighbors = targetNeighbors,
                    )

                    val remoteWeathers = when (remoteWeatherResults) {
                        is Result.Success -> remoteWeatherResults.data
                        is Result.Error -> {
                            send(Result.Error(remoteWeatherResults.error))
                            return@launch
                        }
                    }

                    val (currentList, hourlyList, dailyList) = remoteWeathers.map {
                        it.toWeatherEntity()
                    }.unzip()

                    weatherDatabase.currentWeatherDao.upsertCurrentWeatherList(currentList)
                    weatherDatabase.hourlyWeatherDao.upsertHourlyWeatherList(hourlyList.flatten())
                    weatherDatabase.dailyWeatherDao.upsertDailyWeatherList(dailyList.flatten())
                }


                // load from Database
                val localWeatherFlows = getLocalWeatherFlows(
                    latitude = latitude,
                    longitude = longitude,
                    targetNeighbors = targetNeighbors
                )

                combine(*localWeatherFlows.toTypedArray()) {
                    it.toList()
                }
                .distinctUntilChanged()
                .map { weathers ->
                    val successNeighbors = weathers.map { it.neighbor }
                    val successTargetToWeight = targetToWeight.filterKeys { it in successNeighbors }

                    weightCalculator.calculateWeightedSum(
                        weathers = weathers,
                        targetToWeight = successTargetToWeight
                    )
                }.collect(::send)
            }

        }
    }

    private suspend fun getRemoteWeathers(
        latitude: Double,
        longitude: Double,
        locationName: String,
        targetNeighbors: Set<Neighbor>,
    ): Result<List<Weather>, Error> {
        return withContext(Dispatchers.IO) {
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
                            logger.e { "[error] ${it.error}" }
                            return@withContext Result.Error(it.error)
                        }
                    }
                }
            }

            return@withContext Result.Success(weathers)
        }
    }

    private fun getLocalWeatherFlows(
        latitude: Double,
        longitude: Double,
        targetNeighbors: Set<Neighbor>
    ): List<Flow<Weather>> {
        val now = Clock.System.now().epochSeconds

        val localWeatherListFlows = targetNeighbors.map { neighbor ->
            val currentFlow = weatherDatabase.currentWeatherDao.searchCurrentWeatherListFlow(
                latitude = latitude,
                longitude = longitude,
                neighbor = neighbor.toString(),
                now = now
            ).filter { it.isNotEmpty() }

            val hourlyFlow = weatherDatabase.hourlyWeatherDao.searchHourlyWeatherListFlow(
                latitude = latitude,
                longitude = longitude,
                neighbor = neighbor.toString(),
                now = now
            ).filter { it.isNotEmpty() }

            val dailyFlow = weatherDatabase.dailyWeatherDao.searchDailyWeatherListFlow(
                latitude = latitude,
                longitude = longitude,
                neighbor = neighbor.toString(),
                now = now
            ).filter { it.isNotEmpty() }

            return@map combine(
                currentFlow,
                hourlyFlow,
                dailyFlow
            ) { current, hourlyList, dailyList ->
                Triple(current.last(), hourlyList, dailyList)
            }.map {
                it.toWeather()
            }
        }

        return localWeatherListFlows
    }
}