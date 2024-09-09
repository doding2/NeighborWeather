package weather.data.repository

import co.touchlab.kermit.Logger
import core.util.Error
import core.util.NetworkError
import core.util.Result
import core.util.unzip
import dev.jordond.compass.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import map.domain.model.toLocationName
import weather.data.local.WeatherDatabase
import weather.data.mapper.toWeather
import weather.data.mapper.toWeatherEntity
import weather.data.model.dto.WeatherDto
import weather.data.remote.WeatherClient
import weather.data.util.KoreaWeatherParser.KoreaWeatherParserException
import weather.data.util.WeatherPreprocessor
import weather.data.util.WeatherWeightCalculator
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val weatherDatabase: WeatherDatabase,
    private val weatherClient: WeatherClient,
    private val weatherPreprocessor: WeatherPreprocessor,
    private val weightCalculator: WeatherWeightCalculator
): WeatherRepository {

    private val logger by lazy { Logger.withTag("WeatherRepositoryImpl") }

    override suspend fun fetchWeather(
        place: Place,
        neighbor: Neighbor,
    ): Result<Weather, Error> {
        return withContext(Dispatchers.IO) {
            getRemoteWeathers(
                latitude = place.coordinates.latitude,
                longitude = place.coordinates.longitude,
                locationName = place.toLocationName(),
                targetNeighbors = setOf(neighbor, Neighbor.ALL)
            ).let {
                weatherPreprocessor.preprocess(it).firstOrNull()
                    ?: Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    override suspend fun loadWeathers(
        place: Place,
        targetToWeight: Map<Neighbor, Double>
    ): Flow<Result<Weather, Error>> {
        return withContext(Dispatchers.IO) {
            val locationName = place.toLocationName()

            channelFlow {
                val targetNeighbors = setOf(*targetToWeight.keys.toTypedArray(), Neighbor.ALL)

                // load from Remote API
                launch {
                    val remoteWeatherDtoResults = getRemoteWeathers(
                        latitude = place.coordinates.latitude,
                        longitude = place.coordinates.longitude,
                        locationName = locationName,
                        targetNeighbors = targetNeighbors,
                    )

                    // Weather data from remote call may contains null values.
                    // Fill missing values with best matched weather data.
                    val preprocessedWeatherResults = weatherPreprocessor.preprocess(remoteWeatherDtoResults)

                    val remoteWeathers = preprocessedWeatherResults.mapNotNull {
                        when (it) {
                            is Result.Success -> it.data
                            is Result.Error -> {
                                /*
                                FIXME: MalformedInputException is occurred in
                                 iOS internal charset encoding process randomly.
                                 We can't expect it, so this error is ignored until bug is fixed.
                                 Otherwise, unwrap the result data or return the error.
                                 (MalformedInputException implement KoreaWeatherParserException)
                                 */
                                when (it.error) {
                                    is KoreaWeatherParserException -> {
                                        send(Result.Error(it.error))
                                        null
                                    }
                                    else -> {
                                        send(Result.Error(it.error))
                                        return@launch
                                    }
                                }
                            }
                        }
                    }

                    val (currentList, hourlyList, dailyList) = remoteWeathers.map {
                        it.toWeatherEntity(locationName)
                    }.unzip()

                    weatherDatabase.currentWeatherDao.upsertCurrentWeatherList(currentList)
                    weatherDatabase.hourlyWeatherDao.upsertHourlyWeatherList(hourlyList.flatten())
                    weatherDatabase.dailyWeatherDao.upsertDailyWeatherList(dailyList.flatten())
                }


                // load from Database
                val localWeatherFlows = getLocalWeatherFlows(
                    locationName = locationName,
                    targetNeighbors = targetNeighbors
                )

                combine(*localWeatherFlows.toTypedArray()) {
                    it.toList()
                }
                .distinctUntilChanged()
                .mapNotNull { nullableWeathers ->
                    val weathers = nullableWeathers.filterNotNull()

                    /*
                    When best matched weathers (all neighbor) is not exist,
                    pass this flow iteration and wait for remote weather data.
                     */
                    if (weathers.isEmpty())
                        return@mapNotNull null
                    val containsNeighborAll = Neighbor.ALL in weathers.map { it.neighbor }
                    if (!containsNeighborAll)
                        return@mapNotNull null

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
    ): List<Pair<Neighbor, Result<WeatherDto, Error>>> {
        return withContext(Dispatchers.IO) {
            val weatherDtoResults = targetNeighbors.map { neighbor ->
                async {
                    val result = if (neighbor == Neighbor.Korea) {
                        weatherClient.getKoreaWeather(
                            latitude = latitude,
                            longitude = longitude,
                            locationName = locationName
                        )
                    } else {
                        weatherClient.getNeighborWeather(
                            latitude = latitude,
                            longitude = longitude,
                            neighbor = neighbor
                        )
                    }
                    return@async neighbor to result
                }
            }.awaitAll()

            return@withContext weatherDtoResults
        }
    }

    private fun getLocalWeatherFlows(
        locationName: String,
        targetNeighbors: Set<Neighbor>
    ): List<Flow<Weather?>> {
        val now = Clock.System.now().epochSeconds

        val localWeatherListFlows = targetNeighbors.map { neighbor ->
            val currentFlow = weatherDatabase.currentWeatherDao.searchCurrentWeatherListFlow(
                locationName = locationName,
                neighbor = neighbor.toString(),
            )

            val hourlyFlow = weatherDatabase.hourlyWeatherDao.searchHourlyWeatherListFlow(
                locationName = locationName,
                neighbor = neighbor.toString(),
                now = now
            )

            val dailyFlow = weatherDatabase.dailyWeatherDao.searchDailyWeatherListFlow(
                locationName = locationName,
                neighbor = neighbor.toString(),
                now = now
            )

            return@map combine(
                currentFlow,
                hourlyFlow,
                dailyFlow
            ) { current, hourlyList, dailyList ->
                val lastCurrent = current.lastOrNull() ?: return@combine null
                Triple(lastCurrent, hourlyList, dailyList)
            }.map {
                it?.toWeather()
            }
        }

        return localWeatherListFlows
    }
}