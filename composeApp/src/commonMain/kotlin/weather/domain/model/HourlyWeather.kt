package weather.domain.model

import kotlinx.datetime.LocalDateTime

data class HourlyWeather(
    val time: List<LocalDateTime>,
    val temperature: List<Double>,
    val relativeHumidity: List<Double>,
    val precipitation: List<Double>,
    val weatherCode: List<Int>,
    val windSpeed: List<Double>,
    val windDirection: List<Int>
)
