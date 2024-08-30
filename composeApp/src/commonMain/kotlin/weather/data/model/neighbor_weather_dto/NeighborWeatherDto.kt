package weather.data.model.neighbor_weather_dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import weather.data.remote.model.neighbor_weather.NeighborCurrentDto
import weather.data.remote.model.neighbor_weather.NeighborDailyDto
import weather.data.remote.model.neighbor_weather.NeighborHourlyDto

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
)