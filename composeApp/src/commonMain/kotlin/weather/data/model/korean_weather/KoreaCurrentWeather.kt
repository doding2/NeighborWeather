package weather.data.model.korean_weather

import kotlinx.datetime.LocalDateTime

data class KoreaCurrentWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val relativeHumidity: Double,
    val apparentTemperature: Double,
    val weather: String,
    val windSpeed: Double,
    val windDirection: String
)
