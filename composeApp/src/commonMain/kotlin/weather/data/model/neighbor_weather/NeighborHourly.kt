package weather.data.model.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborHourly(
    @SerialName("time")
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m")
    val temperature2m: List<Double> = emptyList(),
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: List<Double> = emptyList(),
    @SerialName("apparent_temperature")
    val apparentTemperature: List<Double> = emptyList(),
    @SerialName("precipitation")
    val precipitation: List<Double> = emptyList(),
    @SerialName("precipitation_probability")
    val precipitationProbability: List<Double?> = emptyList(),
    @SerialName("weather_code")
    val weatherCode: List<Int> = emptyList(),
    @SerialName("wind_speed_10m")
    val windSpeed10m: List<Double> = emptyList(),
    @SerialName("wind_direction_10m")
    val windDirection10m: List<Double> = emptyList()
)