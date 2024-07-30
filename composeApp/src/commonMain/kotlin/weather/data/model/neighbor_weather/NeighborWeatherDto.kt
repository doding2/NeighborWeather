package weather.data.model.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborWeatherDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("current")
    val current: NeighborCurrent,
    @SerialName("hourly")
    val hourly: NeighborHourly,
    @SerialName("daily")
    val daily: NeighborDaily
)