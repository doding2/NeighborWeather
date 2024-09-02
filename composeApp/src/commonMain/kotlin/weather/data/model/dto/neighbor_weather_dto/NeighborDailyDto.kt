package weather.data.model.dto.neighbor_weather_dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborDailyDto(
    @SerialName("time")
    val time: List<String> = emptyList(),
    @SerialName("weather_code")
    val weatherCode: List<Int?> = emptyList(),
    @SerialName("temperature_2m_max")
    val temperature2mMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min")
    val temperature2mMin: List<Double?> = emptyList(),
    @SerialName("apparent_temperature_max")
    val apparentTemperatureMax: List<Double?> = emptyList(),
    @SerialName("apparent_temperature_min")
    val apparentTemperatureMin: List<Double?> = emptyList(),
    @SerialName("sunrise")
    val sunrise: List<String?> = emptyList(),
    @SerialName("sunset")
    val sunset: List<String?> = emptyList(),
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double?> = emptyList(),
    @SerialName("precipitation_hours")
    val precipitationHours: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m_max")
    val windSpeed10mMax: List<Double?> = emptyList(),
    @SerialName("wind_direction_10m_dominant")
    val windDirection10mDominant: List<Double?> = emptyList()
)