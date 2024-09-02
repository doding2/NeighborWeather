package weather.data.util

import core.util.CommonError
import core.util.Error
import core.util.Result
import core.util.roundToFirst
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
                val mutableTargetWeathers = weathers
                    .filter { it.neighbor in targetToWeight.keys }
                    .toMutableList()

                // In iOS, if MALFORMED_INPUT_EXCEPTION is occurred,
                // korean weather data may not be available.
                // So this may block calculating process.
                val targetNeighbors = mutableTargetWeathers.map { it.neighbor }
//                val isValidTarget = targetToWeight.keys.all { it in targetNeighbors }
//                if (!isValidTarget) {
//                    return@withContext Result.Error(WeatherCalculatorError.INVALID_TARGET_NEIGHBOR)
//                }

//                val weightSum = targetToWeight.values.sum()
                val weightSum = targetToWeight.filterKeys { it in targetNeighbors }.values.sum()
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
                    data = sum.roundToFirst()
                )
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                return@withContext Result.Error(WeatherCalculatorError.CALCULATION_FAILED)
            }
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