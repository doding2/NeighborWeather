package weather.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import weather.data.model.neighbor_weather_dto.NeighborWeatherDto
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
                temperature = temperature2m ?: hourly.temperature2m.getOrNull(0) ?: 0.0,
                relativeHumidity = relativeHumidity2m ?: hourly.relativeHumidity2m.getOrNull(0) ?: 0.0,
                apparentTemperature = apparentTemperature ?: hourly.apparentTemperature.getOrNull(0) ?: 0.0,
                precipitation = precipitation ?: hourly.precipitation.getOrNull(0) ?: 0.0,
                precipitationProbability = hourly.precipitationProbability[0] ?: 0.0,
                weatherCode = weatherCode,
                windSpeed = (windSpeed10m ?: hourly.windSpeed10m.getOrNull(0) ?: 0.0) / UNIT_WIND_SPEED,
                windDirection = windDirection10m ?: hourly.windDirection10m.getOrNull(0) ?: 0.0
            )
        },
        hourly = List(hourly.time.size) { index ->
            HourlyWeather(
                time = hourly.time[index].let { LocalDateTime.parse(it, LocalDateTime.Formats.ISO) },
                temperature = hourly.temperature2m[index],
                relativeHumidity = hourly.relativeHumidity2m[index],
                precipitation = hourly.precipitation[index],
                precipitationProbability = hourly.precipitationProbability[index] ?: -1.0,
                weatherCode = hourly.weatherCode[index] ?: 0,
                windSpeed = hourly.windSpeed10m[index] / UNIT_WIND_SPEED,
                windDirection = hourly.windDirection10m[index]
            )
        },
        daily = List(daily.time.size) { index ->
            DailyWeather(
                time = daily.time[index].let { LocalDate.parse(it, LocalDate.Formats.ISO) },
                temperatureMax = daily.temperature2mMax[index],
                temperatureMin = daily.temperature2mMin[index],
                precipitationProbability = daily.precipitationProbabilityMax[index] ?: -1.0,
                weatherCode = daily.weatherCode[index] ?: 0
            )
        }
    )
}

