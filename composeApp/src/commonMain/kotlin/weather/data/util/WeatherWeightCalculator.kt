package weather.data.util

import core.util.Error
import core.util.Result
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import kotlin.math.max
import kotlin.math.round

class WeatherWeightCalculator {

    fun calculateWeightedSum(
        weathers: List<Weather>,
        targetToWeight: Map<Neighbor, Double>
    ): Result<Weather, Error> {
        val bestWeather = weathers.find { it.neighbor == Neighbor.ALL }
            ?: return Result.Error(WeatherCalculatorError.NO_BEST_WEATHER)
        val mutableTargetWeathers = weathers
            .filter { it.neighbor in targetToWeight.keys }
            .fillMissingValues(bestWeather)
            .toMutableList()

        val targetNeighbors = mutableTargetWeathers.map { it.neighbor }
        val isValidTarget = targetToWeight.keys.all { it in targetNeighbors }
        if (!isValidTarget) {
            return Result.Error(WeatherCalculatorError.INVALID_TARGET_NEIGHBOR)
        }

        val weightSum = targetToWeight.values.sum()
        val firstWeather = mutableTargetWeathers.removeFirstOrNull()
            ?: return Result.Error(WeatherCalculatorError.INDEX_OUT_OF_BOUNDS)
        val firstWeight = round(targetToWeight[firstWeather.neighbor]!! / weightSum * 100) / 100
        val initial = calculateWeight(
            weather = firstWeather,
            weight = firstWeight
        )

        val sum = mutableTargetWeathers.fold(initial) { acc, weather ->
            val weight = round(targetToWeight[weather.neighbor]!! / weightSum * 100) / 100
            val weighted = calculateWeight(weather, weight)

            Weather(
                latitude = acc.latitude,
                longitude = acc.longitude,
                neighbor = Neighbor.ALL,
                current = accumulateCurrentWeather(acc.current, weighted.current),
                hourly = accumulateHourlyWeather(acc.hourly, weighted.hourly),
                daily = accumulateDailyWeather(acc.daily, weighted.daily)
            )
        }

        return Result.Success(sum)
    }

    private fun List<Weather>.fillMissingValues(best: Weather): List<Weather> {
        return map { target ->
            target.copy(
                current = target.current.copy(
                    precipitationProbability = if (target.hourly.precipitationProbability.isEmpty()) {
                        best.current.precipitationProbability
                    } else {
                        target.current.precipitationProbability
                    }
                ),
                hourly = HourlyWeather(
                    time = target.hourly.time.ifEmpty { best.hourly.time },
                    temperature = target.hourly.temperature.ifEmpty { best.hourly.temperature },
                    relativeHumidity = target.hourly.relativeHumidity.ifEmpty { best.hourly.relativeHumidity },
                    precipitation = target.hourly.precipitation.ifEmpty { best.hourly.precipitation },
                    precipitationProbability = target.hourly.precipitationProbability.ifEmpty { best.hourly.precipitationProbability },
                    weatherCode = target.hourly.weatherCode.ifEmpty { best.hourly.weatherCode },
                    windSpeed = target.hourly.windSpeed.ifEmpty { best.hourly.windSpeed },
                    windDirection = target.hourly.windDirection.ifEmpty { best.hourly.windDirection }
                ),
                daily = DailyWeather(
                    time = target.daily.time.ifEmpty { best.daily.time },
                    temperatureMax = target.daily.temperatureMax.ifEmpty { best.daily.temperatureMax },
                    temperatureMin = target.daily.temperatureMin.ifEmpty { best.daily.temperatureMin },
                    precipitationProbability = target.daily.precipitationProbability.ifEmpty { best.daily.precipitationProbability },
                    weatherCode = target.daily.weatherCode.ifEmpty { best.daily.weatherCode }
                )
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
                hourly = hourly.copy(
                    temperature = hourly.temperature.map { it  * weight },
                    relativeHumidity = hourly.relativeHumidity.map { it * weight },
                    precipitation = hourly.precipitation.map { it * weight },
                    precipitationProbability = hourly.precipitationProbability.map { it * weight },
                    windSpeed = hourly.windSpeed.map { it * weight },
                    windDirection = hourly.windDirection.map { it * weight }
                ),
                daily = daily.copy(
                    temperatureMax = daily.temperatureMax.map { it * weight },
                    temperatureMin = daily.temperatureMin.map { it * weight },
                    precipitationProbability = daily.precipitationProbability.map { it * weight },
                )
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
            time = acc.time.zip(add.time) { a, b -> minOf(a, b) },
            temperature = acc.temperature.zip(add.temperature) { a, b -> a + b },
            relativeHumidity = acc.relativeHumidity.zip(add.relativeHumidity) { a, b -> a + b },
            precipitation = acc.precipitation.zip(add.precipitation) { a, b -> a + b },
            precipitationProbability = acc.precipitationProbability.zip(add.precipitationProbability) { a, b -> a + b },
            weatherCode = acc.weatherCode.zip(add.weatherCode) { a, b -> max(a, b) },
            windSpeed = acc.windSpeed.zip(add.windSpeed) { a, b -> a + b },
            windDirection = acc.windDirection.zip(add.windDirection) { a, b -> a + b }
        )
    }

    private fun accumulateDailyWeather(acc: DailyWeather, add: DailyWeather): DailyWeather {
        return DailyWeather(
            time = acc.time.zip(add.time) { a, b -> minOf(a, b)},
            temperatureMax = acc.temperatureMax.zip(add.temperatureMax) { a, b -> a + b },
            temperatureMin = acc.temperatureMin.zip(add.temperatureMin) { a, b -> a + b },
            precipitationProbability = acc.precipitationProbability.zip(add.precipitationProbability) { a, b -> a + b },
            weatherCode = acc.weatherCode.zip(add.weatherCode) { a, b -> max(a, b) }
        )
    }


    enum class WeatherCalculatorError: Error {
        INVALID_TARGET_NEIGHBOR,
        INDEX_OUT_OF_BOUNDS,
        NO_BEST_WEATHER;
    }
}