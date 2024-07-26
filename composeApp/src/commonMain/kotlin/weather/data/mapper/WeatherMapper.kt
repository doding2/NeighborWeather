package weather.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import weather.data.dto.neighbor_weather.NeighborWeatherDto
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Weather

fun NeighborWeatherDto.toWeather(): Weather {
    return Weather(
        latitude = latitude,
        longitude = longitude,
        current = current.run {
            CurrentWeather(
                time = LocalDateTime.parse(time, LocalDateTime.Formats.ISO),
                temperature = temperature2m,
                relativeHumidity = relativeHumidity2m,
                apparentTemperature = apparentTemperature,
                precipitation = precipitation,
                weatherCode = weatherCode
            )
        },
        hourly = hourly.run {
            HourlyWeather(
                time = time.map { LocalDateTime.parse(it, LocalDateTime.Formats.ISO) },
                temperature = temperature2m,
                relativeHumidity = relativeHumidity2m,
                apparentTemperature = apparentTemperature,
                precipitation = precipitation,
                weatherCode = weatherCode
            )
        },
        daily = daily.run {
            DailyWeather(
                time = time.map { LocalDate.parse(it, LocalDate.Formats.ISO) },
                temperatureMean = temperature2mMax.zip(temperature2mMin) { max, min -> (max + min) / 2 },
                apparentTemperatureMean = apparentTemperatureMax.zip(apparentTemperatureMin) { max, min -> (max + min) / 2 },
                sunrise = sunrise.map { LocalDateTime.parse(it) },
                sunset = sunset.map { LocalDateTime.parse(it) },
                precipitationSum = precipitationSum,
                precipitationHours = precipitationHours,
                weatherCode = weatherCode
            )
        }
    )
}