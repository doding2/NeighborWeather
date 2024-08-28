package weather.data.util

import core.util.CommonError
import core.util.Error
import core.util.Result
import core.util.roundToSecond
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import kotlin.math.max

class WeatherWeightCalculator {

    suspend fun calculateWeightedSum(
        weathers: List<Weather>,
        targetToWeight: Map<Neighbor, Double>
    ): Result<Weather, Error> {
        return withContext(Dispatchers.IO) {
            try {
                val bestWeather = weathers.find { it.neighbor == Neighbor.ALL }
                    ?: return@withContext Result.Error(WeatherCalculatorError.NO_BEST_WEATHER)

                val mutableTargetWeathers = weathers
                    .filter { it.neighbor in targetToWeight.keys }
                    .fillMissingValues(bestWeather)
                    .toMutableList()

                val targetNeighbors = mutableTargetWeathers.map { it.neighbor }
                val isValidTarget = targetToWeight.keys.all { it in targetNeighbors }
                if (!isValidTarget) {
                    return@withContext Result.Error(WeatherCalculatorError.INVALID_TARGET_NEIGHBOR)
                }

                val weightSum = targetToWeight.values.sum()
                val firstWeather = mutableTargetWeathers.removeFirstOrNull()
                    ?: return@withContext Result.Error(CommonError.INDEX_OUT_OF_BOUNDS)
                val firstWeight = targetToWeight[firstWeather.neighbor]!! / weightSum
                val initial = calculateWeight(
                    weather = firstWeather,
                    weight = firstWeight
                )

                val sum = mutableTargetWeathers.fold(initial) { acc, weather ->
                    val weight = targetToWeight[weather.neighbor]!! / weightSum
                    val weighted = calculateWeight(weather, weight)


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

                return@withContext Result.Success(
                    data = sum.roundToSecond()
                )
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                return@withContext Result.Error(WeatherCalculatorError.CALCULATION_FAILED)
            }
        }
    }

    private fun List<Weather>.fillMissingValues(best: Weather): List<Weather> {
        return map { target ->
            target.copy(
                current = target.current.copy(
                    precipitationProbability = if (target.hourly.map { it.precipitationProbability }.isEmpty()) {
                        best.current.precipitationProbability
                    } else {
                        target.current.precipitationProbability
                    }
                ),
                hourly = target.hourly
                    .mapIndexed { index, item ->
                        item.copy(
                            precipitationProbability = if (item.precipitationProbability == -1.0) {
                                best.hourly.getOrNull(index)?.precipitationProbability ?: 0.0
                            } else {
                                item.precipitationProbability
                            }
                        )
                    }.ifEmpty { best.hourly },
                daily = target.daily
                    .mapIndexed { index, item ->
                        item.copy(
                            precipitationProbability = if (item.precipitationProbability == -1.0) {
                                best.daily.getOrNull(index)?.precipitationProbability ?: 0.0
                            } else {
                                item.precipitationProbability
                            }
                        )
                    }.ifEmpty { best.daily }
            )
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
        return CurrentWeather(
            time = minOf(acc.time, add.time),
            temperature = acc.temperature + add.temperature,
            relativeHumidity = acc.relativeHumidity + add.relativeHumidity,
            apparentTemperature = acc.apparentTemperature + add.apparentTemperature,
            precipitation = acc.precipitation + add.precipitation,
            precipitationProbability = acc.precipitationProbability + add.precipitationProbability,
            weatherCode = max(acc.weatherCode, add.weatherCode),
            windSpeed = acc.windSpeed + add.windSpeed,
            windDirection = acc.windDirection + add.windDirection
        )
    }

    private fun accumulateHourlyWeather(acc: HourlyWeather, add: HourlyWeather): HourlyWeather {
        return HourlyWeather(
            time = minOf(acc.time, add.time),
            temperature = acc.temperature + add.temperature,
            relativeHumidity = acc.relativeHumidity + add.relativeHumidity,
            precipitation = acc.precipitation + add.precipitation,
            precipitationProbability = acc.precipitationProbability + add.precipitationProbability,
            weatherCode = max(acc.weatherCode, add.weatherCode) ,
            windSpeed = acc.windSpeed + add.windSpeed,
            windDirection = acc.windDirection + add.windDirection
        )
    }

    private fun accumulateDailyWeather(acc: DailyWeather, add: DailyWeather): DailyWeather {
        return DailyWeather(
            time = minOf(acc.time, add.time),
            temperatureMax = acc.temperatureMax + add.temperatureMax,
            temperatureMin = acc.temperatureMin + add.temperatureMin,
            precipitationProbability = acc.precipitationProbability + add.precipitationProbability,
            weatherCode = max(acc.weatherCode, add.weatherCode)
        )
    }

    private fun Weather.roundToSecond(): Weather {
        return this.copy(
            latitude = latitude,
            longitude = longitude,
            neighbor = neighbor,
            current = current.copy(
                time = current.time,
                temperature = roundToSecond(current.temperature),
                relativeHumidity = roundToSecond(current.relativeHumidity),
                apparentTemperature = roundToSecond(current.apparentTemperature),
                precipitation = roundToSecond(current.precipitation),
                precipitationProbability = roundToSecond(current.precipitationProbability),
                windSpeed = roundToSecond(current.windSpeed),
                windDirection = roundToSecond(current.windDirection)
            ),
            hourly = hourly.map {
                it.copy(
                    temperature = roundToSecond(it.temperature),
                    relativeHumidity = roundToSecond(it.relativeHumidity),
                    precipitation = roundToSecond(it.precipitation),
                    precipitationProbability = roundToSecond(it.precipitationProbability),
                    windSpeed = roundToSecond(it.windSpeed),
                    windDirection = roundToSecond(it.windDirection)
                )
            },
            daily = daily.map {
                it.copy(
                    temperatureMax = roundToSecond(it.temperatureMax),
                    temperatureMin = roundToSecond(it.temperatureMin),
                    precipitationProbability = roundToSecond(it.precipitationProbability),
                )
            }
        )
    }


    enum class WeatherCalculatorError: Error {
        INVALID_TARGET_NEIGHBOR,
        NO_BEST_WEATHER,
        CALCULATION_FAILED;
    }
}