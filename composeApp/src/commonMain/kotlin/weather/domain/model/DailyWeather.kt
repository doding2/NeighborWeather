package weather.domain.model

import kotlinx.datetime.LocalDate

data class DailyWeather(
    val time: List<LocalDate>,
    val temperatureMax: List<Double>,
    val temperatureMin: List<Double>,
    val precipitationProbability: List<Double>,
    val weatherCode: List<Int>,
)
