package weather.data.model.dto.neighbor_weather_dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborCurrentDto(
    @SerialName("time")
    val time: String,
    @SerialName("temperature_2m")
    val temperature2m: Double? = null,
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: Double? = null,
    @SerialName("apparent_temperature")
    val apparentTemperature: Double? = null,
    @SerialName("precipitation")
    val precipitation: Double? = null,
    @SerialName("weather_code")
    val weatherCode: Int? = null,
    @SerialName("wind_speed_10m")
    val windSpeed10m: Double? = null,
    @SerialName("wind_direction_10m")
    val windDirection10m: Double? = null
)