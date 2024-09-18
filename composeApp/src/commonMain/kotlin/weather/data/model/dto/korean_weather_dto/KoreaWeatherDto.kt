package weather.data.model.dto.korean_weather_dto

data class KoreaWeatherDto(
    val latitude: Double,
    val longitude: Double,
    val current: KoreaCurrentWeatherDto,
    val hourly: KoreaHourlyWeatherDto,
    val daily: KoreaDailyWeatherDto
)