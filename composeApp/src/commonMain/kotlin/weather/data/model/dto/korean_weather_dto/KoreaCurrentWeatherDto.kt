package weather.data.model.dto.korean_weather_dto

import kotlinx.datetime.LocalDateTime

data class KoreaCurrentWeatherDto(
    val time: LocalDateTime,
    val temperature: Double,
    val precipitation: Double,
    val relativeHumidity: Double,
    val apparentTemperature: Double,
    val weather: String,
    val windSpeed: Double,
    val windDirection: String
)
