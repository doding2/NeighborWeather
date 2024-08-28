package weather.data.remote.model.korean_weather

import kotlinx.datetime.LocalDate

data class KoreaDailyWeatherDto(
    val time: List<LocalDate>,
    val temperatureMax: List<Double>,
    val temperatureMin: List<Double>,
    val precipitationProbabilityAM: List<Double>,
    val precipitationProbabilityPM: List<Double>,
    val weatherAM: List<String>,
    val weatherPM: List<String>,
)