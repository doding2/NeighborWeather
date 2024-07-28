package weather.data.model.korean_weather

data class KoreaWeather(
    val latitude: Double,
    val longitude: Double,
    val current: KoreaCurrentWeather,
    val hourly: KoreaHourlyWeather,
    val daily: KoreaDailyWeather
)