package weather.data.mapper

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import weather.data.model.weather_entity.CurrentWeatherEntity
import weather.data.model.weather_entity.DailyWeatherEntity
import weather.data.model.weather_entity.HourlyWeatherEntity
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Weather

fun Weather.toWeatherEntity(): Triple<CurrentWeatherEntity, List<HourlyWeatherEntity>, List<DailyWeatherEntity>> {
    val current = CurrentWeatherEntity(
        latitude = latitude,
        longitude = longitude,
        neighbor = neighbor,
        epochTime = current.time.toInstant(TimeZone.currentSystemDefault()) .epochSeconds,
        temperature = current.temperature,
        relativeHumidity = current.relativeHumidity,
        apparentTemperature = current.apparentTemperature,
        precipitation = current.precipitation,
        precipitationProbability = current.precipitation,
        weatherCode = current.weatherCode,
        windSpeed = current.windSpeed,
        windDirection = current.windDirection
    )
    val hourlyList = hourly.map {
        HourlyWeatherEntity(
            latitude = latitude,
            longitude = longitude,
            neighbor = neighbor,
            epochTime = it.time.toInstant(TimeZone.currentSystemDefault()).epochSeconds,
            temperature = it.temperature,
            relativeHumidity = it.relativeHumidity,
            precipitation = it.precipitation,
            precipitationProbability =
                if (it.precipitationProbability == -1.0) null
                else it.precipitationProbability,
            weatherCode = it.weatherCode,
            windSpeed = it.windSpeed,
            windDirection = it.windDirection
        )
    }
    val dailyList = daily.map {
        DailyWeatherEntity(
            latitude = latitude,
            longitude = longitude,
            neighbor = neighbor,
            epochTime = LocalDateTime(it.time.year, it.time.monthNumber, it.time.dayOfMonth, 0, 0, 0)
                .toInstant(TimeZone.currentSystemDefault()).epochSeconds,
            temperatureMax = it.temperatureMax,
            temperatureMin = it.temperatureMin,
            precipitationProbability =
            if (it.precipitationProbability == -1.0) null
            else it.precipitationProbability,
            weatherCode = it.weatherCode
        )
    }
    return Triple(current, hourlyList, dailyList)
}

fun Triple<CurrentWeatherEntity, List<HourlyWeatherEntity>, List<DailyWeatherEntity>>.toWeather(): Weather {
    return Weather(
        latitude = first.latitude,
        longitude = first.longitude,
        neighbor = first.neighbor,
        current = CurrentWeather(
            time = Instant.fromEpochSeconds(first.epochTime)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            temperature = first.temperature,
            relativeHumidity = first.relativeHumidity,
            apparentTemperature = first.apparentTemperature,
            precipitation = first.precipitation,
            precipitationProbability = first.precipitationProbability,
            weatherCode = first.weatherCode,
            windSpeed = first.windSpeed,
            windDirection = first.windDirection
        ),
        hourly = second.map {
            HourlyWeather(
                time = Instant.fromEpochSeconds(it.epochTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                temperature = it.temperature,
                relativeHumidity = it.relativeHumidity,
                precipitation = it.precipitation,
                precipitationProbability = it.precipitationProbability ?: -1.0,
                weatherCode = it.weatherCode,
                windSpeed = it.windSpeed,
                windDirection = it.windDirection
            )
        },
        daily = third.map {
            DailyWeather(
                time = Instant.fromEpochSeconds(it.epochTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date,
                temperatureMax = it.temperatureMax,
                temperatureMin = it.temperatureMin,
                precipitationProbability = it.precipitationProbability ?: -1.0,
                weatherCode = it.weatherCode
            )
        }

    )
}