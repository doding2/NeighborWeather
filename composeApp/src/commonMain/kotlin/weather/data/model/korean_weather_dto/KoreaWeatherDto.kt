package weather.data.remote.model.korean_weather

data class KoreaWeatherDto(
    val latitude: Double,
    val longitude: Double,
    val current: KoreaCurrentWeatherDto,
    val hourly: KoreaHourlyWeatherDto,
    val daily: KoreaDailyWeatherDto
)