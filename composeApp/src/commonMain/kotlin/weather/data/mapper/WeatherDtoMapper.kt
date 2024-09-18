package weather.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.byUnicodePattern
import weather.data.model.dto.korean_weather_dto.KoreaWeatherDto
import weather.data.model.dto.neighbor_weather_dto.NeighborCurrentDto
import weather.data.model.dto.neighbor_weather_dto.NeighborDailyDto
import weather.data.model.dto.neighbor_weather_dto.NeighborHourlyDto
import weather.data.model.dto.neighbor_weather_dto.NeighborWeatherDto
import weather.domain.model.toWeatherCode
import kotlin.math.max

fun KoreaWeatherDto.toNeighborWeatherDto(): NeighborWeatherDto {
    val unitDegree = 22.5
    val windDirectionMap = mapOf(
        "북풍" to 0,
        "북북동풍" to 1,
        "북동풍" to 2,
        "동북동풍" to 3,
        "동풍" to 4,
        "동남동풍" to 5,
        "남동풍" to 6,
        "남남동풍" to 7,
        "남풍" to 8,
        "남남서풍" to 9,
        "남서풍" to 10,
        "서남서풍" to 11,
        "서풍" to 12,
        "서북서풍" to 13,
        "북서풍" to 14,
        "북북서풍" to 15
    ).mapValues { it.value * unitDegree }
    val datetimeFormat = LocalDateTime.Format {
        byUnicodePattern("yyyy-MM-dd'T'HH:mm")
    }
    return NeighborWeatherDto(
        latitude = latitude,
        longitude = longitude,
        current = NeighborCurrentDto(
            time = current.time.format(datetimeFormat),
            temperature2m = current.temperature,
            relativeHumidity2m = current.relativeHumidity,
            precipitation = current.precipitation,
            weatherCode = current.weather.toWeatherCode(),
            windSpeed10m = current.windSpeed,
            windDirection10m = windDirectionMap.let { map ->
                map[current.windDirection]
                    ?: map[hourly.windDirection.getOrElse(0) { map.keys.first() }]!!
            }
        ),
        hourly = NeighborHourlyDto(
            time = hourly.time.map { it.format(datetimeFormat) },
            temperature2m = hourly.temperature,
            relativeHumidity2m = hourly.relativeHumidity,
            precipitation = hourly.precipitation,
            precipitationProbability = hourly.precipitationProbability,
            weatherCode = hourly.weather.map { it.toWeatherCode() },
            windSpeed10m = hourly.windSpeed,
            windDirection10m = hourly.windDirection.map {
                windDirectionMap[it] ?: windDirectionMap.values.first()
            }
        ),
        daily = NeighborDailyDto(
            time = daily.time.map { it.format(LocalDate.Formats.ISO) },
            weatherCode = daily.weatherAM.zip(daily.weatherPM) { am, pm ->
                max(am.toWeatherCode() ?: 0, pm.toWeatherCode() ?: 0)
            },
            temperature2mMax = daily.temperatureMax,
            temperature2mMin = daily.temperatureMin,
            precipitationProbabilityMax = daily.precipitationProbabilityAM
                .zip(daily.precipitationProbabilityPM) { am, pm ->
                    max(am, pm)
                }
        )
    )
}