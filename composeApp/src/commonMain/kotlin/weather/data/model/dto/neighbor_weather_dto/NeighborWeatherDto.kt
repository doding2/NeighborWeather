package weather.data.model.dto.neighbor_weather_dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import weather.data.model.dto.WeatherDto

@Serializable
data class NeighborWeatherDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("current")
    val current: NeighborCurrentDto,
    @SerialName("hourly")
    val hourly: NeighborHourlyDto,
    @SerialName("daily")
    val daily: NeighborDailyDto
): WeatherDto