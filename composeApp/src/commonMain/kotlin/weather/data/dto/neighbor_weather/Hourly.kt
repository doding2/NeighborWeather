package weather.data.dto.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Hourly(
    @SerialName("time")
    val time: List<String>,
    @SerialName("temperature_2m")
    val temperature2m: List<Double>,
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: List<Double>,
    @SerialName("apparent_temperature")
    val apparentTemperature: List<Double>,
    @SerialName("precipitation")
    val precipitation: List<Double>,
    @SerialName("weather_code")
    val weatherCode: List<Int>
)