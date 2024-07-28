package weather.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import weather.data.model.neighbor_weather.NeighborWeatherDto
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather

fun NeighborWeatherDto.toWeather(neighbor: Neighbor): Weather {
    val UNIT_WIND_SPEED = 3.6
    return Weather(
        latitude = latitude,
        longitude = longitude,
        neighbor = neighbor,
        current = current.run {
            CurrentWeather(
                time = LocalDateTime.parse(time, LocalDateTime.Formats.ISO),
                temperature = temperature2m,
                relativeHumidity = relativeHumidity2m,
                apparentTemperature = apparentTemperature,
                precipitation = precipitation,
                precipitationProbability = hourly.precipitationProbability[0] ?: 0.0,
                weatherCode = weatherCode,
                windSpeed = windSpeed10m / UNIT_WIND_SPEED,
                windDirection = windDirection10m.toDouble()
            )
        },
        hourly = hourly.run {
            HourlyWeather(
                time = time.map { LocalDateTime.parse(it, LocalDateTime.Formats.ISO) },
                temperature = temperature2m,
                relativeHumidity = relativeHumidity2m,
                precipitation = precipitation,
                precipitationProbability = precipitationProbability.filterNotNull(),
                weatherCode = weatherCode,
                windSpeed = windSpeed10m.map { it / UNIT_WIND_SPEED },
                windDirection = windDirection10m.map { it.toDouble() }
            )
        },
        daily = daily.run {
            DailyWeather(
                time = time.map { LocalDate.parse(it, LocalDate.Formats.ISO) },
                temperatureMax = temperature2mMax,
                temperatureMin = temperature2mMin,
                precipitationProbability = precipitationProbabilityMax.filterNotNull(),
                weatherCode = weatherCode,
            )
        }
    )
}

