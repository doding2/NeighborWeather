package weather.data.model.dto.neighbor_weather_dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborDailyUnits(
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
    val precipitationHours: String,
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: String,
    @SerialName("wind_speed_10m_max")
    val windSpeed10mMax: String,
    @SerialName("wind_direction_10m_dominant")
    val windDirection10mDominant: String
)