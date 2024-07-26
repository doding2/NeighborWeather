package weather.data.dto.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HourlyUnits(
    @SerialName("time")
    val time: String,
    @SerialName("temperature_2m")
    val temperature2m: String,
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: String,
    @SerialName("apparent_temperature")
    val apparentTemperature: String,
    @SerialName("precipitation")
    val precipitation: String,
    @SerialName("weather_code")
    val weatherCode: String
)