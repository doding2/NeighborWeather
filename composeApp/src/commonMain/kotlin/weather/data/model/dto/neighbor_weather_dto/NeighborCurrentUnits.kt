package weather.data.model.dto.neighbor_weather_dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborCurrentUnits(
    @SerialName("time")
    val time: String,
    @SerialName("interval")
    val interval: String,
    @SerialName("temperature_2m")
    val temperature2m: String,
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: String,
    @SerialName("apparent_temperature")
    val apparentTemperature: String,
    @SerialName("precipitation")
    val precipitation: String,
    @SerialName("weather_code")
    val weatherCode: String,
    @SerialName("wind_speed_10m")
    val windSpeed10m: String,
    @SerialName("wind_direction_10m")
    val windDirection10m: String
)