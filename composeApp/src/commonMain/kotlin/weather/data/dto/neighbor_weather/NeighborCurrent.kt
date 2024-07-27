package weather.data.dto.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborCurrent(
    @SerialName("time")
    val time: String,
    @SerialName("interval")
    val interval: Int,
    @SerialName("temperature_2m")
    val temperature2m: Double,
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: Double,
    @SerialName("apparent_temperature")
    val apparentTemperature: Double,
    @SerialName("precipitation")
    val precipitation: Double,
    @SerialName("weather_code")
    val weatherCode: Int,
    @SerialName("wind_speed_10m")
    val windSpeed10m: Double,
    @SerialName("wind_direction_10m")
    val windDirection10m: Int
)