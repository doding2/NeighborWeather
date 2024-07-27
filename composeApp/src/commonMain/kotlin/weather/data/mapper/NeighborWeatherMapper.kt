package weather.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import weather.data.dto.neighbor_weather.NeighborWeatherDto
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather

fun NeighborWeatherDto.toWeather(neighbor: Neighbor): Weather {
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
                weatherCode = weatherCode,
                windSpeed = windSpeed10m,
                windDirection = windDirection10m
            )
        },
        hourly = hourly.run {
            HourlyWeather(
                time = time.map { LocalDateTime.parse(it, LocalDateTime.Formats.ISO) },
                temperature = temperature2m,
                relativeHumidity = relativeHumidity2m,
                precipitation = precipitation,
                weatherCode = weatherCode,
                windSpeed = windSpeed10m,
                windDirection = windDirection10m
            )
        },
        daily = daily.run {
            DailyWeather(
                time = time.map { LocalDate.parse(it, LocalDate.Formats.ISO) },
                temperatureMax = temperature2mMax,
                temperatureMin = temperature2mMin,
                sunrise = sunrise.map { LocalDateTime.parse(it) },
                sunset = sunset.map { LocalDateTime.parse(it) },
                precipitationSum = precipitationSum,
                precipitationHours = precipitationHours,
                weatherCode = weatherCode,
                windSpeed = windSpeed10mMax,
                windDirection = windDirection10mDominant
            )
        }
    )
}

