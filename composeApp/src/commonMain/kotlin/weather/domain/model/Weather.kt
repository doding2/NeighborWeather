package weather.domain.model

data class Weather(
    val latitude: Double,
    val longitude: Double,
    val neighbors: List<Neighbor>,
    val current: CurrentWeather,
    val hourly: HourlyWeather,
    val daily: DailyWeather
)