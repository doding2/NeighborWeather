package weather.domain.model

import kotlinx.datetime.LocalDateTime

data class CurrentWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val relativeHumidity: Int,
    val apparentTemperature: Double,
    val precipitation: Double,
    val weatherCode: Int,
)
