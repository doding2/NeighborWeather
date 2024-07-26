package weather.data.dto.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Current(
    @SerialName("time")
    val time: String,
    @SerialName("interval")
    val interval: Int,
    @SerialName("temperature_2m")
    val temperature2m: Double,
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: Int,
    @SerialName("apparent_temperature")
    val apparentTemperature: Double,
    @SerialName("precipitation")
    val precipitation: Double,
    @SerialName("weather_code")
    val weatherCode: Int
)