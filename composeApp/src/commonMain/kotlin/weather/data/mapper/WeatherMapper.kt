package weather.data.mapper

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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
                time = LocalDateTime.parse(time, LocalDateTime.Formats.ISO)
                    .toInstant(TimeZone.of("Asia/Tokyo"))
                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                temperature = temperature2m,
                relativeHumidity = relativeHumidity2m,
                apparentTemperature = apparentTemperature,
                precipitation = precipitation,
                weatherCode = weatherCode
            )
        },
        hourly = hourly.run {
            HourlyWeather(
                time = time.map {
                    LocalDateTime.parse(it, LocalDateTime.Formats.ISO)
                        .toInstant(TimeZone.of("Asia/Tokyo"))
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                },
                temperature = temperature2m,
                relativeHumidity = relativeHumidity2m,
                apparentTemperature = apparentTemperature,
                precipitation = precipitation,
                weatherCode = weatherCode
            )
        },
        daily = daily.run {
            DailyWeather(
                time = time.map {
                    LocalDateTime.parse(it, LocalDateTime.Formats.ISO)
                        .toInstant(TimeZone.of("Asia/Tokyo"))
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                },
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