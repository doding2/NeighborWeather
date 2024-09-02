package weather.data.util

import core.util.Error
import core.util.Result
import core.util.getDataOrNull
import core.util.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import weather.data.model.dto.WeatherDto
import weather.data.model.dto.korean_weather_dto.KoreaWeatherDto
import weather.data.model.dto.neighbor_weather_dto.NeighborWeatherDto
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max

class WeatherPreprocessor {

    fun preprocess(results: List<Pair<Neighbor, Result<WeatherDto, Error>>>): List<Result<Weather, Error>> {
        // Weather data from remote call may contains null values.
        // Fill missing values with best matched weather data.
        // And convert to Weather object.
        val bestWeatherDto = results
            .find { it.first == Neighbor.ALL }
            ?.second
            ?.getDataOrNull() as? NeighborWeatherDto
        if (bestWeatherDto == null) {
            return listOf(
                Result.Error(
                    WeatherPreprocessorError.NO_BEST_WEATHER
                )
            )
        }
        return results.map outerMap@{ (neighbor, result) ->
            result.map { weatherDto ->
                when (weatherDto) {
                    is KoreaWeatherDto -> mapToWeather(weatherDto)
                    is NeighborWeatherDto -> {
                        val filled = runCatching {
                            fillMissingValues(weatherDto, bestWeatherDto)
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
                    else -> return@outerMap Result.Error(
                        WeatherPreprocessorError.UNKNOWN_WEATHER_DTO_TYPE
                    )
                }
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
                copy(
                    temperature2m = temperature2m.mapIndexed { index, value ->
                        value ?: best.hourly.temperature2m[index]!!
                    },
                    relativeHumidity2m = relativeHumidity2m.mapIndexed { index, value ->
                        value ?: best.hourly.relativeHumidity2m[index]!!
                    },
                    apparentTemperature = apparentTemperature.mapIndexed { index, value ->
                        value ?: best.hourly.apparentTemperature[index]!!
                    },
                    precipitation = precipitation.mapIndexed { index, value ->
                        value ?: best.hourly.precipitation[index]!!
                    },
                    precipitationProbability = precipitationProbability.mapIndexed { index, value ->
                        value ?: best.hourly.precipitationProbability[index]!!
                    },
                    weatherCode = weatherCode.mapIndexed { index, value ->
                        value ?: best.hourly.weatherCode[index]!!
                    },
                    windSpeed10m = windSpeed10m.mapIndexed { index, value ->
                        value ?: best.hourly.windSpeed10m[index]!!
                    },
                    windDirection10m = windDirection10m.mapIndexed { index, value ->
                        value ?: best.hourly.windDirection10m[index]!!
                    }
                )
            },
            daily = target.daily.run {
                copy(
                    weatherCode = weatherCode.mapIndexed { index, value ->
                        value ?: best.daily.weatherCode[index]!!
                    },
                    temperature2mMax = temperature2mMax.mapIndexed { index, value ->
                        value ?: best.daily.temperature2mMax[index]!!
                    },
                    temperature2mMin = temperature2mMin.mapIndexed { index, value ->
                        value ?: best.daily.temperature2mMin[index]!!
                    },
                    apparentTemperatureMax = apparentTemperatureMax.mapIndexed { index, value ->
                        value ?: best.daily.apparentTemperatureMax[index]!!
                    },
                    apparentTemperatureMin = apparentTemperatureMin.mapIndexed { index, value ->
                        value ?: best.daily.apparentTemperatureMin[index]!!
                    },
                    sunrise = sunrise.mapIndexed { index, value ->
                        value ?: best.daily.sunrise[index]!!
                    },
                    sunset = sunset.mapIndexed { index, value ->
                        value ?: best.daily.sunset[index]!!
                    },
                    precipitationSum = precipitationSum.mapIndexed { index, value ->
                        value ?: best.daily.precipitationSum[index]!!
                    },
                    precipitationHours = precipitationHours.mapIndexed { index, value ->
                        value ?: best.daily.precipitationHours[index]!!
                    },
                    precipitationProbabilityMax = precipitationProbabilityMax.mapIndexed { index, value ->
                        value ?: best.daily.precipitationProbabilityMax[index]!!
                    },
                    windSpeed10mMax = windSpeed10mMax.mapIndexed { index, value ->
                        value ?: best.daily.windSpeed10mMax[index]!!
                    },
                    windDirection10mDominant = windDirection10mDominant.mapIndexed { index, value ->
                        value ?: best.daily.windDirection10mDominant[index]!!
                    }
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
                    windSpeed = windSpeed10m!! / UNIT_WIND_SPEED,
                    windDirection = windDirection10m!!
                )
            },
            hourly = List(weatherDto.hourly.time.size) { index ->
                HourlyWeather(
                    time = weatherDto.hourly.time[index].let { LocalDateTime.parse(it, LocalDateTime.Formats.ISO) },
                    temperature = weatherDto.hourly.temperature2m[index]!!,
                    relativeHumidity = weatherDto.hourly.relativeHumidity2m[index]!!,
                    precipitation = weatherDto.hourly.precipitation[index]!!,
                    precipitationProbability = weatherDto.hourly.precipitationProbability[index]!!,
                    weatherCode = weatherDto.hourly.weatherCode[index]!!,
                    windSpeed = (weatherDto.hourly.windSpeed10m[index]!!) / UNIT_WIND_SPEED,
                    windDirection = weatherDto.hourly.windDirection10m[index]!!
                )
            },
            daily = List(weatherDto.daily.time.size) { index ->
                DailyWeather(
                    time = weatherDto.daily.time[index].let { LocalDate.parse(it, LocalDate.Formats.ISO) },
                    temperatureMax = weatherDto.daily.temperature2mMax[index]!!,
                    temperatureMin = weatherDto.daily.temperature2mMin[index]!!,
                    precipitationProbability = weatherDto.daily.precipitationProbabilityMax[index]!!,
                    weatherCode = weatherDto.daily.weatherCode[index]!!
                )
            }
        )
    }

    private fun mapToWeather(koreaWeatherDto: KoreaWeatherDto): Weather {
        val weatherMap = mapOf(
            "맑음" to 0,
            "대체로 화창" to 0,   // 아큐웨더 (overseas)
            "일부 화창" to 0,   // 아큐웨더 (overseas)
            "화창" to 0,   // 아큐웨더 (overseas)
            "구름조금" to 0,
            "비" to 65,
            "소나기" to 65,   // 아큐웨더 (overseas)
            "번개뇌우" to 65,   // 아큐웨더 (overseas)
            "일부 흐림/소나기" to 65,   // 아큐웨더 (overseas)
            "구름많음" to 2,
            "흐림" to 3,
            "흐리고 한때 비" to 61,
            "흐리고 비" to 63,
            "구름많고 한때 비 곳" to 61,
            "구름많고 한때 비" to 61,
            "구름많고 비" to 63,
            "눈" to 75,
            "흐리고 한때 눈" to 71,
            "흐리고 눈" to 73,
            "구름많고 한때 눈 곳" to 71,
            "구름많고 한때 눈" to 71,
            "구름많고 눈" to 73,
        )
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
                CurrentWeather(
                    time = time,
                    temperature = temperature,
                    relativeHumidity = relativeHumidity,
                    apparentTemperature = apparentTemperature,
                    precipitation = precipitation,
                    precipitationProbability = koreaWeatherDto.hourly.precipitationProbability[0],
                    weatherCode = weatherMap[weather]
                        ?: weatherMap.entries.reversed()
                            .firstOrNull { weather.contains(it.key) }?.value
                        ?: 0,
                    windSpeed = windSpeed,
                    windDirection = windDirectionMap.let { map ->
                        map[windDirection]
                            ?: map[koreaWeatherDto.hourly.windDirection.getOrElse(0) { map.keys.first() }]!!
                    }
                )
            },
            hourly = List(koreaWeatherDto.hourly.time.size) { index ->
                HourlyWeather(
                    time = koreaWeatherDto.hourly.time[index],
                    temperature = koreaWeatherDto.hourly.temperature[index],
                    relativeHumidity = koreaWeatherDto.hourly.relativeHumidity[index],
                    precipitation = koreaWeatherDto.hourly.precipitation[index],
                    precipitationProbability = koreaWeatherDto.hourly.precipitationProbability[index],
                    weatherCode = weatherMap[koreaWeatherDto.hourly.weather[index]]
                        ?: weatherMap.entries.reversed()
                            .firstOrNull { koreaWeatherDto.current.weather.contains(it.key) }?.value
                        ?: 0,
                    windSpeed = koreaWeatherDto.hourly.windSpeed[index],
                    windDirection = windDirectionMap[koreaWeatherDto.hourly.windDirection[index]] ?: windDirectionMap.values.first()
                )
            },
            daily = List(koreaWeatherDto.daily.time.size) { index ->
                DailyWeather(
                    time = koreaWeatherDto.daily.time[index],
                    temperatureMax = koreaWeatherDto.daily.temperatureMax[index],
                    temperatureMin = koreaWeatherDto.daily.temperatureMin[index],
                    precipitationProbability = max(
                        koreaWeatherDto.daily.precipitationProbabilityAM[index],
                        koreaWeatherDto.daily.precipitationProbabilityPM[index]
                    ),
                    weatherCode = max(
                        weatherMap[koreaWeatherDto.daily.weatherAM[index]]
                            ?: weatherMap.entries.reversed()
                                .firstOrNull { koreaWeatherDto.current.weather.contains(it.key) }?.value
                            ?: 0,
                        weatherMap[koreaWeatherDto.daily.weatherPM[index]]
                            ?: weatherMap.entries.reversed()
                                .firstOrNull { koreaWeatherDto.current.weather.contains(it.key) }?.value
                            ?: 0
                    )
                )
            }
        )
    }

    enum class WeatherPreprocessorError: Error {
        NO_BEST_WEATHER,
        UNKNOWN_WEATHER_DTO_TYPE,
        FAIL_TO_FILL_MISSING_VALUES,
        FAIL_TO_MAP_WEATHER
    }
}