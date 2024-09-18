package weather.data.util

import co.touchlab.kermit.Logger
import core.domain.util.Error
import core.domain.util.Result
import core.domain.util.getDataOrNull
import core.domain.util.map
import dev.jordond.compass.Place
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import weather.data.model.dto.korean_weather_dto.KoreaWeatherDto
import weather.data.model.dto.neighbor_weather_dto.NeighborWeatherDto
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import weather.domain.model.toNeighbor
import weather.domain.model.toWeatherCode
import weather.domain.model.toWeatherType
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max

class WeatherPreprocessor {

    private val logger by lazy { Logger.withTag("WeatherPreprocessor") }

    fun preprocess(results: List<Pair<Neighbor, Result<NeighborWeatherDto, Error>>>, place: Place): List<Result<Weather, Error>> {
        // Weather data from remote call may contains null values.
        // Fill missing values with Local Meteorological Administrations
        // and best matched weather data.
        // And convert to Weather object.
        val bestWeatherDto = results
            .find { it.first == Neighbor.ALL }
            ?.second
            ?.getDataOrNull()
        if (bestWeatherDto == null) {
            return listOf(
                Result.Error(
                    WeatherPreprocessorError.FAIL_TO_LOAD_BEST_WEATHER
                )
            )
        }
        val myLocationWeatherDto = results
            .find { it.first == place.toNeighbor() }
            ?.second
            ?.getDataOrNull()
            ?.let {
                runCatching {
                    fillMissingValues(it, bestWeatherDto)
                }.getOrNull()
            } ?: bestWeatherDto

        return results.map outerMap@{ (neighbor, result) ->
            result.map { weatherDto ->
                val filled = runCatching {
                    fillMissingValues(weatherDto, myLocationWeatherDto)
                }.getOrElse {
                    if (it is CancellationException) throw it
                    it.printStackTrace()
                    return@outerMap Result.Error(
                        WeatherPreprocessorError.FAIL_TO_FILL_MISSING_VALUES
                    )
                }
                val weather = runCatching {
                    mapToWeather(neighbor, filled)
                }.getOrElse {
                    if (it is CancellationException) throw it
                    it.printStackTrace()
                    return@outerMap Result.Error(
                        WeatherPreprocessorError.FAIL_TO_MAP_WEATHER
                    )
                }
                weather
            }
        }
    }

    fun fillMissingValues(target: NeighborWeatherDto, best: NeighborWeatherDto): NeighborWeatherDto {
        return target.copy(
            current = target.current.run {
                copy(
                    temperature2m = temperature2m
                        ?: target.hourly.temperature2m.getOrNull(0)
                        ?: best.current.temperature2m!!,
                    relativeHumidity2m = relativeHumidity2m
                        ?: target.hourly.relativeHumidity2m.getOrNull(0)
                        ?: best.current.relativeHumidity2m!!,
                    apparentTemperature = apparentTemperature
                        ?: target.hourly.apparentTemperature.getOrNull(0)
                        ?: best.current.apparentTemperature!!,
                    precipitation = precipitation
                        ?: target.hourly.precipitation.getOrNull(0)
                        ?: best.current.precipitation!!,
                    weatherCode = weatherCode
                        ?: target.hourly.weatherCode.getOrNull(0)
                        ?: best.current.weatherCode!!,
                    windSpeed10m = windSpeed10m
                        ?: target.hourly.windSpeed10m.getOrNull(0)
                        ?: best.current.windSpeed10m!!,
                    windDirection10m = windDirection10m
                        ?: target.hourly.windDirection10m.getOrNull(0)
                        ?: best.current.windDirection10m!!,
                )
            },
            hourly = target.hourly.run {
                val targetSize = target.hourly.time.size
                val bestSize = best.hourly.time.size
                var targetStartIndex = 0
                var bestStartIndex = 0
                if (targetSize >= bestSize) {
                    val firstTarget = best.hourly.time.firstOrNull() ?: target.hourly.time.first()
                    targetStartIndex = target.hourly.time.indexOfFirst { it == firstTarget }
                } else {
                    val firstTarget = target.hourly.time.firstOrNull() ?: best.hourly.time.first()
                    bestStartIndex = best.hourly.time.indexOfFirst { it == firstTarget }
                }
                copy(
                    time = if (targetSize >= bestSize) best.hourly.time else time,
                    temperature2m = if (temperature2m.isEmpty()) best.hourly.temperature2m.subList(bestStartIndex, bestSize)
                        else temperature2m.subList(targetStartIndex, targetSize).zip(best.hourly.temperature2m.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                    relativeHumidity2m = if (relativeHumidity2m.isEmpty()) best.hourly.relativeHumidity2m.subList(bestStartIndex, bestSize)
                        else relativeHumidity2m.subList(targetStartIndex, targetSize).zip(best.hourly.relativeHumidity2m.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                    apparentTemperature = if (apparentTemperature.isEmpty()) best.hourly.apparentTemperature.subList(bestStartIndex, bestSize)
                        else apparentTemperature.subList(targetStartIndex, targetSize).zip(best.hourly.apparentTemperature.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                    precipitation = if (precipitation.isEmpty()) best.hourly.precipitation.subList(bestStartIndex, bestSize)
                        else precipitation.subList(targetStartIndex, targetSize).zip(best.hourly.precipitation.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                    precipitationProbability = if (precipitationProbability.isEmpty()) best.hourly.precipitationProbability.subList(bestStartIndex, bestSize)
                        else precipitationProbability.subList(targetStartIndex, targetSize).zip(best.hourly.precipitationProbability.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                    weatherCode = if (weatherCode.isEmpty()) best.hourly.weatherCode.subList(bestStartIndex, bestSize)
                        else weatherCode.subList(targetStartIndex, targetSize).zip(best.hourly.weatherCode.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                    windSpeed10m = if (windSpeed10m.isEmpty()) best.hourly.windSpeed10m.subList(bestStartIndex, bestSize)
                        else windSpeed10m.subList(targetStartIndex, targetSize).zip(best.hourly.windSpeed10m.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                    windDirection10m = if (windDirection10m.isEmpty()) best.hourly.windDirection10m.subList(bestStartIndex, bestSize)
                        else windDirection10m.subList(targetStartIndex, targetSize).zip(best.hourly.windDirection10m.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                )
            },
            daily = target.daily.run {
                val targetSize = target.daily.time.size
                val bestSize = best.daily.time.size
                var targetStartIndex = 0
                var bestStartIndex = 0
                if (targetSize >= bestSize) {
                    val firstTarget = best.daily.time.firstOrNull() ?: target.daily.time.first()
                    targetStartIndex = target.daily.time.indexOfFirst { it == firstTarget }
                } else {
                    val firstTarget = target.daily.time.firstOrNull() ?: best.daily.time.first()
                    bestStartIndex = best.daily.time.indexOfFirst { it == firstTarget }
                }
                copy(
                    time = if (targetSize >= bestSize) best.daily.time else time,
                    weatherCode = if (weatherCode.isEmpty()) best.daily.weatherCode.subList(bestStartIndex, bestSize)
                        else weatherCode.subList(targetStartIndex, targetSize).zip(best.daily.weatherCode.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!! },
                    temperature2mMax = if (temperature2mMax.isEmpty()) best.daily.temperature2mMax.subList(bestStartIndex, bestSize)
                        else temperature2mMax.subList(targetStartIndex, targetSize).zip(best.daily.temperature2mMax.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    temperature2mMin = if (temperature2mMin.isEmpty()) best.daily.temperature2mMin.subList(bestStartIndex, bestSize)
                        else temperature2mMin.subList(targetStartIndex, targetSize).zip(best.daily.temperature2mMin.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    apparentTemperatureMax = if (apparentTemperatureMax.isEmpty()) best.daily.apparentTemperatureMax.subList(bestStartIndex, bestSize)
                        else apparentTemperatureMax.subList(targetStartIndex, targetSize).zip(best.daily.apparentTemperatureMax.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    apparentTemperatureMin = if (apparentTemperatureMin.isEmpty()) best.daily.apparentTemperatureMin.subList(bestStartIndex, bestSize)
                        else apparentTemperatureMin.subList(targetStartIndex, targetSize).zip(best.daily.apparentTemperatureMin.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    sunrise = if (sunrise.isEmpty()) best.daily.sunrise.subList(bestStartIndex, bestSize)
                        else sunrise.subList(targetStartIndex, targetSize).zip(best.daily.sunrise.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    sunset = if (sunset.isEmpty()) best.daily.sunset.subList(bestStartIndex, bestSize)
                        else sunset.subList(targetStartIndex, targetSize).zip(best.daily.sunset.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    precipitationSum = if (precipitationSum.isEmpty()) best.daily.precipitationSum.subList(bestStartIndex, bestSize)
                        else precipitationSum.subList(targetStartIndex, targetSize).zip(best.daily.precipitationSum.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    precipitationHours = if (precipitationHours.isEmpty()) best.daily.precipitationHours.subList(bestStartIndex, bestSize)
                        else precipitationHours.subList(targetStartIndex, targetSize).zip(best.daily.precipitationHours.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    precipitationProbabilityMax = if (precipitationProbabilityMax.isEmpty()) best.daily.precipitationProbabilityMax.subList(bestStartIndex, bestSize)
                        else precipitationProbabilityMax.subList(targetStartIndex, targetSize).zip(best.daily.precipitationProbabilityMax.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    windSpeed10mMax = if (windSpeed10mMax.isEmpty()) best.daily.windSpeed10mMax.subList(bestStartIndex, bestSize)
                        else windSpeed10mMax.subList(targetStartIndex, targetSize).zip(best.daily.windSpeed10mMax.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                    windDirection10mDominant = if (windDirection10mDominant.isEmpty()) best.daily.windDirection10mDominant.subList(bestStartIndex, bestSize)
                        else windDirection10mDominant.subList(targetStartIndex, targetSize).zip(best.daily.windDirection10mDominant.subList(bestStartIndex, bestSize)) { target, best -> target ?: best!!},
                )
            }
        )
    }

    private fun mapToWeather(neighbor: Neighbor, weatherDto: NeighborWeatherDto): Weather {
        val UNIT_WIND_SPEED = 3.6
        return Weather(
            latitude = weatherDto.latitude,
            longitude = weatherDto.longitude,
            neighbor = neighbor,
            current = weatherDto.current.run {
                CurrentWeather(
                    time = LocalDateTime.parse(time, LocalDateTime.Formats.ISO),
                    temperature = temperature2m!!,
                    relativeHumidity = relativeHumidity2m!!,
                    apparentTemperature = apparentTemperature!!,
                    precipitation = precipitation!!,
                    precipitationProbability = weatherDto.hourly.precipitationProbability[0]!!,
                    weatherCode = weatherCode!!,
                    weatherType = weatherCode.toWeatherType(),
                    windSpeed = windSpeed10m!! / UNIT_WIND_SPEED,
                    windDirection = windDirection10m!!
                )
            },
            hourly = List(weatherDto.hourly.time.size) { index ->
                val weatherCode = weatherDto.hourly.weatherCode[index]!!
                HourlyWeather(
                    time = weatherDto.hourly.time[index].let { LocalDateTime.parse(it, LocalDateTime.Formats.ISO) },
                    temperature = weatherDto.hourly.temperature2m[index]!!,
                    relativeHumidity = weatherDto.hourly.relativeHumidity2m[index]!!,
                    precipitation = weatherDto.hourly.precipitation[index]!!,
                    precipitationProbability = weatherDto.hourly.precipitationProbability[index]!!,
                    weatherCode = weatherCode,
                    weatherType = weatherCode.toWeatherType(),
                    windSpeed = (weatherDto.hourly.windSpeed10m[index]!!) / UNIT_WIND_SPEED,
                    windDirection = weatherDto.hourly.windDirection10m[index]!!
                )
            },
            daily = List(weatherDto.daily.time.size) { index ->
                val weatherCode = weatherDto.daily.weatherCode[index]!!
                DailyWeather(
                    time = weatherDto.daily.time[index].let { LocalDate.parse(it, LocalDate.Formats.ISO) },
                    temperatureMax = weatherDto.daily.temperature2mMax[index]!!,
                    temperatureMin = weatherDto.daily.temperature2mMin[index]!!,
                    precipitationProbability = weatherDto.daily.precipitationProbabilityMax[index]!!,
                    weatherCode = weatherCode,
                    weatherType = weatherCode.toWeatherType(),
                )
            }
        )
    }


    private fun mapToWeather(koreaWeatherDto: KoreaWeatherDto): Weather {
        val unitDegree = 22.5
        val windDirectionMap = mapOf(
            "북풍" to 0,
            "북북동풍" to 1,
            "북동풍" to 2,
            "동북동풍" to 3,
            "동풍" to 4,
            "동남동풍" to 5,
            "남동풍" to 6,
            "남남동풍" to 7,
            "남풍" to 8,
            "남남서풍" to 9,
            "남서풍" to 10,
            "서남서풍" to 11,
            "서풍" to 12,
            "서북서풍" to 13,
            "북서풍" to 14,
            "북북서풍" to 15
        ).mapValues { it.value * unitDegree }
        return Weather(
            latitude = koreaWeatherDto.latitude,
            longitude = koreaWeatherDto.longitude,
            neighbor = Neighbor.Korea,
            current = koreaWeatherDto.current.run {
                val weatherCode = weather.toWeatherCode() ?: 0
                CurrentWeather(
                    time = time,
                    temperature = temperature,
                    relativeHumidity = relativeHumidity,
                    apparentTemperature = apparentTemperature,
                    precipitation = precipitation,
                    precipitationProbability = koreaWeatherDto.hourly.precipitationProbability[0],
                    weatherCode = weatherCode,
                    weatherType = weatherCode.toWeatherType(),
                    windSpeed = windSpeed,
                    windDirection = windDirectionMap.let { map ->
                        map[windDirection]
                            ?: map[koreaWeatherDto.hourly.windDirection.getOrElse(0) { map.keys.first() }]!!
                    }
                )
            },
            hourly = List(koreaWeatherDto.hourly.time.size) { index ->
                val weatherCode = koreaWeatherDto.hourly.weather[index]
                    .toWeatherCode() ?: 0
                HourlyWeather(
                    time = koreaWeatherDto.hourly.time[index],
                    temperature = koreaWeatherDto.hourly.temperature[index],
                    relativeHumidity = koreaWeatherDto.hourly.relativeHumidity[index],
                    precipitation = koreaWeatherDto.hourly.precipitation[index],
                    precipitationProbability = koreaWeatherDto.hourly.precipitationProbability[index],
                    weatherCode = weatherCode,
                    weatherType = weatherCode.toWeatherType(),
                    windSpeed = koreaWeatherDto.hourly.windSpeed[index],
                    windDirection = windDirectionMap[koreaWeatherDto.hourly.windDirection[index]] ?: windDirectionMap.values.first()
                )
            },
            daily = List(koreaWeatherDto.daily.time.size) { index ->
                val weatherAMCode = koreaWeatherDto.daily.weatherAM[index].toWeatherCode() ?: 0
                val weatherPMCode = koreaWeatherDto.daily.weatherPM[index].toWeatherCode() ?: 0
                val weatherCode = max(weatherAMCode, weatherPMCode)
                DailyWeather(
                    time = koreaWeatherDto.daily.time[index],
                    temperatureMax = koreaWeatherDto.daily.temperatureMax[index],
                    temperatureMin = koreaWeatherDto.daily.temperatureMin[index],
                    precipitationProbability = max(
                        koreaWeatherDto.daily.precipitationProbabilityAM[index],
                        koreaWeatherDto.daily.precipitationProbabilityPM[index]
                    ),
                    weatherCode = weatherCode,
                    weatherType = weatherCode.toWeatherType()
                )
            }
        )
    }

    enum class WeatherPreprocessorError: Error {
        FAIL_TO_LOAD_BEST_WEATHER,
        UNKNOWN_WEATHER_DTO_TYPE,
        FAIL_TO_FILL_MISSING_VALUES,
        FAIL_TO_MAP_WEATHER
    }
}