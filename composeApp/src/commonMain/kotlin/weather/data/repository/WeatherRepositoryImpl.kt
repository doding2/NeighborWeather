package weather.data.repository

import co.touchlab.kermit.Logger
import core.domain.util.Error
import core.domain.util.NetworkError
import core.domain.util.Result
import core.domain.util.unzip
import dev.jordond.compass.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import map.domain.util.toPlaceIdentifier
import weather.data.local.WeatherDatabase
import weather.data.mapper.toWeather
import weather.data.mapper.toWeatherEntity
import weather.data.model.dto.neighbor_weather_dto.NeighborWeatherDto
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

    override suspend fun fetchRemoteWeather(
        place: Place,
        neighbor: Neighbor,
    ): Result<Weather, Error> {
        return withContext(Dispatchers.IO) {
            fetchRemoteWeathers(place, setOf(neighbor, Neighbor.ALL))
                .let { weatherPreprocessor.preprocess(it).firstOrNull() }
                ?: Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun loadWeathers(
        place: Place,
        neighborWeights: Map<Neighbor, Double>,
        fetchFromRemote: Boolean
    ): Flow<Result<Weather, Error>> {
        return withContext(Dispatchers.IO) {
            val locationName = place.toPlaceIdentifier()
            val targetNeighbors = setOf(*neighborWeights.keys.toTypedArray(), Neighbor.ALL)

            channelFlow {
                // load from Remote API
                if (fetchFromRemote) {
                    launch {
                        this@channelFlow.fetchAndUpdateRemoteWeathers(place, neighborWeights)
                    }
                }

                // load from Database
                val localWeatherFlows = getLocalWeatherFlows(locationName, targetNeighbors)

                combine(*localWeatherFlows.toTypedArray()) { it }
                    .mapNotNull { nullableWeathers ->
                        val weathers = nullableWeathers.filterNotNull()
                        val successNeighbors = weathers.map { it.neighbor }
                        val successTargetToWeight =
                            neighborWeights.filterKeys { it in successNeighbors }
                        if (weathers.none { it.neighbor in successTargetToWeight.keys }) {
                            return@mapNotNull null
                        }

                        weightCalculator.calculateWeightedSum(weathers, successTargetToWeight)
                    }
                    .distinctUntilChanged()
                    .collect(::send)
            }
        }
    }

    private suspend fun ProducerScope<Result<Weather, Error>>.fetchAndUpdateRemoteWeathers(
        place: Place,
        neighborWeights: Map<Neighbor, Double>
    ) {
        return withContext(Dispatchers.IO) {
            val locationName = place.toPlaceIdentifier()
            val topWeightedNeighbor = neighborWeights.maxBy { it.value }.key
            val targetNeighbors = setOf(*neighborWeights.keys.toTypedArray(), Neighbor.ALL)

            val remoteWeatherDtoResults = fetchRemoteWeathers(place, targetNeighbors)
            val preprocessedWeatherResults = weatherPreprocessor.preprocess(
                results = remoteWeatherDtoResults,
                topWeightedNeighbor = topWeightedNeighbor
            )

            val remoteWeathers = preprocessedWeatherResults.mapNotNull {
                when (it) {
                    is Result.Success -> it.data
                    is Result.Error -> {
                        send(Result.Error(it.error))
                        /*
                        FIXME: MalformedInputException is occurred in
                         iOS internal charset encoding process randomly.
                         We can't expect it, so this error is ignored until bug is fixed.
                         Otherwise, unwrap the result data or return the error.
                         (MalformedInputException implement KoreaWeatherParserException)
                         */
                        when (it.error) {
                            is KoreaWeatherParserException -> null
                            else -> return@withContext
                        }
                    }
                }
            }

            val (currentList, hourlyList, dailyList) =
                remoteWeathers.map { it.toWeatherEntity(locationName) }.unzip()
                    .run { Triple(first, second.flatten(), third.flatten()) }

            weatherDatabase.run {
                // upsert new data
                currentWeatherDao.upsertCurrentWeatherList(currentList)
                hourlyWeatherDao.upsertHourlyWeatherList(hourlyList)
                dailyWeatherDao.upsertDailyWeatherList(dailyList)

                // delete old data
                currentList.minOfOrNull { it.epochTime }?.let {
                    currentWeatherDao.deletePastCurrentWeather(locationName, it)
                }
                hourlyList.minOfOrNull { it.epochTime }?.let {
                    hourlyWeatherDao.deletePastHourlyWeather(locationName, it)
                }
                dailyList.minOfOrNull { it.epochTime }?.let {
                    dailyWeatherDao.deletePastDailyWeather(locationName, it)
                }
            }
        }
    }

    private suspend fun fetchRemoteWeathers(
        place: Place,
        targetNeighbors: Set<Neighbor>,
    ): List<Pair<Neighbor, Result<NeighborWeatherDto, Error>>> {
        val locationName = place.toPlaceIdentifier()
        return withContext(Dispatchers.IO) {
            targetNeighbors.map { neighbor ->
                async {
                    val result = if (neighbor == Neighbor.Korea) {
                        weatherClient.getKoreaWeather(
                            latitude = place.coordinates.latitude,
                            longitude = place.coordinates.longitude,
                            locationName = locationName
                        )
                    } else {
                        weatherClient.getNeighborWeather(
                            latitude = place.coordinates.latitude,
                            longitude = place.coordinates.longitude,
                            neighbor = neighbor
                        )
                    }
                    neighbor to result
                }
            }.awaitAll()
        }
    }

    private fun getLocalWeatherFlows(
        locationName: String,
        targetNeighbors: Set<Neighbor>
    ): List<Flow<Weather?>> {
        val now = Clock.System.now().epochSeconds

        return targetNeighbors.map { neighbor ->
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

            combine(currentFlow, hourlyFlow, dailyFlow) { current, hourlyList, dailyList ->
                val lastCurrent = current.lastOrNull() ?: return@combine null
                Triple(lastCurrent, hourlyList, dailyList)
            }.map { it?.toWeather() }
        }
    }
}