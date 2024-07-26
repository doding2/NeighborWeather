package weather.data.dto.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyUnits(
    @SerialName("time")
    val time: String,
    @SerialName("weather_code")
    val weatherCode: String,
    @SerialName("temperature_2m_max")
    val temperature2mMax: String,
    @SerialName("temperature_2m_min")
    val temperature2mMin: String,
    @SerialName("apparent_temperature_max")
    val apparentTemperatureMax: String,
    @SerialName("apparent_temperature_min")
    val apparentTemperatureMin: String,
    @SerialName("sunrise")
    val sunrise: String,
    @SerialName("sunset")
    val sunset: String,
    @SerialName("precipitation_sum")
    val precipitationSum: String,
    @SerialName("precipitation_hours")
    val precipitationHours: String
)