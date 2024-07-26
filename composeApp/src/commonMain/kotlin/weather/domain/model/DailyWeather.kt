package weather.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class DailyWeather(
    val time: List<LocalDate>,
    val temperatureMean: List<Double>,
    val apparentTemperatureMean: List<Double>,
    val sunrise: List<LocalDateTime>,
    val sunset: List<LocalDateTime>,
    val precipitationSum: List<Double>,
    val precipitationHours: List<Int>,
    val weatherCode: List<Int>
)
