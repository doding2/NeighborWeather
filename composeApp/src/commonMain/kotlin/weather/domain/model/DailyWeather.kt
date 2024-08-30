package weather.domain.model

import kotlinx.datetime.LocalDate

data class DailyWeather(
    val time: LocalDate,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val precipitationProbability: Double,
    val weatherCode: Int,
)
