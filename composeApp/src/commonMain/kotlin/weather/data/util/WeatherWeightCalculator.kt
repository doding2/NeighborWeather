package weather.data.util

import co.touchlab.kermit.Logger
import core.domain.util.CommonError
import core.domain.util.Error
import core.domain.util.Result
import core.domain.util.roundToFirst
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import weather.domain.model.WeatherType
import weather.domain.model.toWeatherCode
import weather.domain.model.toWeatherType
import kotlin.math.max

class WeatherWeightCalculator {

    private val logger by lazy { Logger.withTag("WeatherWeightCalculator") }

    suspend fun calculateWeightedSum(
        weathers: List<Weather>,
        targetToWeight: Map<Neighbor, Double>
    ): Result<Weather, Error> {
        return withContext(Dispatchers.Default) {
            try {
                val mutableWeathers = weathers
                    .filter { it.neighbor in targetToWeight.keys }
                    .toMutableList()

                val targetNeighbors = mutableWeathers.map { it.neighbor }
                val weightSum = targetToWeight.filterKeys { it in targetNeighbors }.values.sum()
                val firstWeather = mutableWeathers.removeFirstOrNull()
                    ?: return@withContext Result.Error(CommonError.INDEX_OUT_OF_BOUNDS)
                val firstWeight = targetToWeight[firstWeather.neighbor]!! / weightSum
                val initial = calculateWeight(
                    weather = firstWeather,
                    weight = firstWeight
                )

                // pick best weather code by weighted voting (categorical data)
                val currentWeatherCodeMap = mutableSetOf<Pair<WeatherType, Double>>()
                val hourlyWeatherCodeMaps = mutableListOf<MutableSet<Pair<WeatherType, Double>>>()
                val dailyWeatherCodeMaps = mutableListOf<MutableSet<Pair<WeatherType, Double>>>()
                updateWeatherCodeMaps(
                    weather = firstWeather,
                    weight = firstWeight,
                    currentWeatherCodeMap = currentWeatherCodeMap,
                    hourlyWeatherCodeMaps = hourlyWeatherCodeMaps,
                    dailyWeatherCodeMaps = dailyWeatherCodeMaps
                )

                // calculate numeric data by summing with weights
                val sumNumeric = mutableWeathers.fold(initial) { acc, weather ->
                    val weight = targetToWeight[weather.neighbor]!! / weightSum
                    val weighted = calculateWeight(weather, weight)

                    updateWeatherCodeMaps(
                        weather = weather,
                        weight = weight,
                        currentWeatherCodeMap = currentWeatherCodeMap,
                        hourlyWeatherCodeMaps = hourlyWeatherCodeMaps,
                        dailyWeatherCodeMaps = dailyWeatherCodeMaps
                    )

                    Weather(
                        latitude = acc.latitude,
                        longitude = acc.longitude,
                        neighbor = Neighbor.ALL,
                        current = accumulateCurrentWeather(acc.current, weighted.current),
                        hourly =  acc.hourly.zip(weighted.hourly) { a, w ->
                            accumulateHourlyWeather(a, w)
                        },
                        daily = acc.daily.zip(weighted.daily) { a, w ->
                            accumulateDailyWeather(a, w)
                        }
                    )
                }

                // apply picked best weather codes (categorical data)
                val (currentWeatherType, hourlyWeatherTypes, dailyWeatherTypes) =
                    pickBestWeatherCodes(currentWeatherCodeMap, hourlyWeatherCodeMaps, dailyWeatherCodeMaps)
                val sumAll = sumNumeric.run {
                    copy(
                        current = current.copy(
                            weatherCode = currentWeatherType?.toWeatherCode() ?: current.weatherCode,
                            weatherType = currentWeatherType ?: current.weatherType
                        ),
                        hourly = hourly.mapIndexed { index, hourlyWeather ->
                            hourlyWeather.copy(
                                weatherCode = hourlyWeatherTypes[index]?.toWeatherCode() ?: hourlyWeather.weatherCode,
                                weatherType = hourlyWeatherTypes[index] ?: hourlyWeather.weatherType
                            )
                        },
                        daily = daily.mapIndexed { index, dailyWeather ->
                            dailyWeather.copy(
                                weatherCode = dailyWeatherTypes[index]?.toWeatherCode() ?: dailyWeather.weatherCode,
                                weatherType = dailyWeatherTypes[index] ?: dailyWeather.weatherType
                            )
                        }
                    )
                }

                return@withContext Result.Success(
                    data = sumAll.roundToFirst()
                )
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                return@withContext Result.Error(WeatherCalculatorError.CALCULATION_FAILED)
            }
        }
    }

    private fun pickBestWeatherCodes(
        currentWeatherCodeMap: Set<Pair<WeatherType, Double>>,
        hourlyWeatherCodeMaps: List<Set<Pair<WeatherType, Double>>>,
        dailyWeatherCodeMaps: List<Set<Pair<WeatherType, Double>>>,
    ): Triple<WeatherType?, List<WeatherType?>, List<WeatherType?>> {
        val currentWeatherCode = currentWeatherCodeMap.maxByOrNull { it.second }?.first
        val hourlyWeatherCodes = hourlyWeatherCodeMaps.map { map -> map.maxByOrNull { it.second }?.first }
        val dailyWeatherCodes = dailyWeatherCodeMaps.map { map -> map.maxByOrNull { it.second }?.first }
        return Triple(currentWeatherCode, hourlyWeatherCodes, dailyWeatherCodes)
    }

    private fun updateWeatherCodeMaps(
        weather: Weather,
        weight: Double,
        currentWeatherCodeMap: MutableSet<Pair<WeatherType, Double>>,
        hourlyWeatherCodeMaps: MutableList<MutableSet<Pair<WeatherType, Double>>>,
        dailyWeatherCodeMaps: MutableList<MutableSet<Pair<WeatherType, Double>>>,
    ) {
        // update current weather code map
        updateWeatherCodeMap(
            weatherType = weather.current.weatherCode.toWeatherType(),
            weight = weight,
            weatherCodeMap = currentWeatherCodeMap
        )

        // update hourly weather code map
        weather.hourly.forEachIndexed { index, hourlyWeather ->
            val map = hourlyWeatherCodeMaps.getOrElse(index) {
                val newMap = mutableSetOf<Pair<WeatherType, Double>>()
                hourlyWeatherCodeMaps.add(newMap)
                newMap
            }
            updateWeatherCodeMap(
                weatherType = hourlyWeather.weatherCode.toWeatherType(),
                weight = weight,
                weatherCodeMap = map
            )
        }

        // update daily weather code map
        weather.daily.forEachIndexed { index, dailyWeather ->
            val map = dailyWeatherCodeMaps.getOrElse(index) {
                val newMap = mutableSetOf<Pair<WeatherType, Double>>()
                dailyWeatherCodeMaps.add(newMap)
                newMap
            }
            updateWeatherCodeMap(
                weatherType = dailyWeather.weatherCode.toWeatherType(),
                weight = weight,
                weatherCodeMap = map
            )
        }
    }

    private fun updateWeatherCodeMap(
        weatherType: WeatherType,
        weight: Double,
        weatherCodeMap: MutableSet<Pair<WeatherType, Double>>
    ) {
        val weatherCodeToWeight = weatherCodeMap.find { it.first ==  weatherType}
        if (weatherCodeToWeight == null) {
            weatherCodeMap.add(weatherType to weight)
        } else {
            weatherCodeMap.remove(weatherCodeToWeight)
            weatherCodeMap.add(weatherCodeToWeight.first to  weatherCodeToWeight.second + weight)
        }
    }


    private fun calculateWeight(weather: Weather, weight: Double): Weather {
        return weather.run {
            copy(
                neighbor = Neighbor.ALL,
                current = current.copy(
                    temperature = current.temperature * weight,
                    relativeHumidity = current.relativeHumidity * weight,
                    apparentTemperature = current.apparentTemperature * weight,
                    precipitation = current.precipitation * weight,
                    precipitationProbability = current.precipitationProbability * weight,
                    windSpeed = current.windSpeed * weight,
                    windDirection = current.windDirection * weight
                ),
                hourly = hourly.map {
                    it.copy(
                        temperature = it.temperature  * weight,
                        relativeHumidity = it.relativeHumidity * weight,
                        precipitation = it.precipitation * weight,
                        precipitationProbability = it.precipitationProbability * weight,
                        windSpeed = it.windSpeed * weight,
                        windDirection = it.windDirection * weight
                    )
                },
                daily = daily.map {
                    it.copy(
                        temperatureMax = it.temperatureMax * weight,
                        temperatureMin = it.temperatureMin * weight,
                        precipitationProbability = it.precipitationProbability * weight
                    )
                }
            )
        }
    }

    private fun accumulateCurrentWeather(acc: CurrentWeather, add: CurrentWeather): CurrentWeather {
        val weatherCode = max(acc.weatherCode, add.weatherCode)
        return CurrentWeather(
            time = minOf(acc.time, add.time),
            temperature = acc.temperature + add.temperature,
            relativeHumidity = acc.relativeHumidity + add.relativeHumidity,
            apparentTemperature = acc.apparentTemperature + add.apparentTemperature,
            precipitation = acc.precipitation + add.precipitation,
            precipitationProbability = acc.precipitationProbability + add.precipitationProbability,
            weatherCode = weatherCode,
            weatherType = weatherCode.toWeatherType(),
            windSpeed = acc.windSpeed + add.windSpeed,
            windDirection = acc.windDirection + add.windDirection
        )
    }

    private fun accumulateHourlyWeather(acc: HourlyWeather, add: HourlyWeather): HourlyWeather {
        val weatherCode = max(acc.weatherCode, add.weatherCode)
        return HourlyWeather(
            time = minOf(acc.time, add.time),
            temperature = acc.temperature + add.temperature,
            relativeHumidity = acc.relativeHumidity + add.relativeHumidity,
            precipitation = acc.precipitation + add.precipitation,
            precipitationProbability = acc.precipitationProbability + add.precipitationProbability,
            weatherCode = weatherCode,
            weatherType = weatherCode.toWeatherType(),
            windSpeed = acc.windSpeed + add.windSpeed,
            windDirection = acc.windDirection + add.windDirection
        )
    }

    private fun accumulateDailyWeather(acc: DailyWeather, add: DailyWeather): DailyWeather {
        val weatherCode = max(acc.weatherCode, add.weatherCode)
        return DailyWeather(
            time = minOf(acc.time, add.time),
            temperatureMax = acc.temperatureMax + add.temperatureMax,
            temperatureMin = acc.temperatureMin + add.temperatureMin,
            precipitationProbability = acc.precipitationProbability + add.precipitationProbability,
            weatherCode = weatherCode,
            weatherType = weatherCode.toWeatherType()
        )
    }

    private fun Weather.roundToFirst(): Weather {
        return this.copy(
            latitude = latitude,
            longitude = longitude,
            neighbor = neighbor,
            current = current.copy(
                time = current.time,
                temperature = roundToFirst(current.temperature),
                relativeHumidity = roundToFirst(current.relativeHumidity),
                apparentTemperature = roundToFirst(current.apparentTemperature),
                precipitation = roundToFirst(current.precipitation),
                precipitationProbability = roundToFirst(current.precipitationProbability),
                windSpeed = roundToFirst(current.windSpeed),
                windDirection = roundToFirst(current.windDirection)
            ),
            hourly = hourly.map {
                it.copy(
                    temperature = roundToFirst(it.temperature),
                    relativeHumidity = roundToFirst(it.relativeHumidity),
                    precipitation = roundToFirst(it.precipitation),
                    precipitationProbability = roundToFirst(it.precipitationProbability),
                    windSpeed = roundToFirst(it.windSpeed),
                    windDirection = roundToFirst(it.windDirection)
                )
            },
            daily = daily.map {
                it.copy(
                    temperatureMax = roundToFirst(it.temperatureMax),
                    temperatureMin = roundToFirst(it.temperatureMin),
                    precipitationProbability = roundToFirst(it.precipitationProbability),
                )
            }
        )
    }


    enum class WeatherCalculatorError: Error {
        INVALID_TARGET_NEIGHBOR,
        CALCULATION_FAILED;
    }
}
