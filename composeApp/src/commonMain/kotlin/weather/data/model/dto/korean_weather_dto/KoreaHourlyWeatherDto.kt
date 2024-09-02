package weather.data.model.dto.korean_weather_dto

import kotlinx.datetime.LocalDateTime

data class KoreaHourlyWeatherDto(
    val time: List<LocalDateTime>,
    val temperature: List<Double>,
    val relativeHumidity: List<Double>,
    val precipitation: List<Double>,
    val precipitationProbability: List<Double>,
    val weather: List<String>,
    val windSpeed: List<Double>,
    val windDirection: List<String>
)
