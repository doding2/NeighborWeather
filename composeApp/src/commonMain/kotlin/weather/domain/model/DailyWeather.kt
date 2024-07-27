package weather.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class DailyWeather(
    val time: List<LocalDate>,
    val temperatureMax: List<Double>,
    val temperatureMin: List<Double>,
    val sunrise: List<LocalDateTime>,
    val sunset: List<LocalDateTime>,
    val precipitationSum: List<Double>,
    val precipitationHours: List<Double>,
    val weatherCode: List<Int>,
    val windSpeed: List<Double>,
    val windDirection: List<Int>
)
