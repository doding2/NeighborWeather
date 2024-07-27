package weather.data.dto.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborDaily(
    @SerialName("time")
    val time: List<String>,
    @SerialName("weather_code")
    val weatherCode: List<Int>,
    @SerialName("temperature_2m_max")
    val temperature2mMax: List<Double>,
    @SerialName("temperature_2m_min")
    val temperature2mMin: List<Double>,
    @SerialName("apparent_temperature_max")
    val apparentTemperatureMax: List<Double>,
    @SerialName("apparent_temperature_min")
    val apparentTemperatureMin: List<Double>,
    @SerialName("sunrise")
    val sunrise: List<String>,
    @SerialName("sunset")
    val sunset: List<String>,
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double>,
    @SerialName("precipitation_hours")
    val precipitationHours: List<Double>,
    @SerialName("wind_speed_10m_max")
    val windSpeed10mMax: List<Double>,
    @SerialName("wind_direction_10m_dominant")
    val windDirection10mDominant: List<Int>
)