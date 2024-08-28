package weather.domain.model

import kotlinx.datetime.LocalDateTime

data class HourlyWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val relativeHumidity: Double,
    val precipitation: Double,
    val precipitationProbability: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val windDirection: Double
)
