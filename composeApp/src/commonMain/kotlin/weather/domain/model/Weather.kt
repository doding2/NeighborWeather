package weather.domain.model

data class Weather(
    val latitude: Double,
    val longitude: Double,
    val neighbor: Neighbor,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>
)