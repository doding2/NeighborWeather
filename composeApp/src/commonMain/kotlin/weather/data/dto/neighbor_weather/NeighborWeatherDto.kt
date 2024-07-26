package weather.data.dto.neighbor_weather


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeighborWeatherDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("generationtime_ms")
    val generationTimeMs: Double,
    @SerialName("utc_offset_seconds")
    val utcOffsetSeconds: Int,
    @SerialName("timezone")
    val timezone: String,
    @SerialName("timezone_abbreviation")
    val timezoneAbbreviation: String,
    @SerialName("elevation")
    val elevation: Double,
    @SerialName("current_units")
    val currentUnits: CurrentUnits,
    @SerialName("current")
    val current: Current,
    @SerialName("hourly_units")
    val hourlyUnits: HourlyUnits,
    @SerialName("hourly")
    val hourly: Hourly,
    @SerialName("daily_units")
    val dailyUnits: DailyUnits,
    @SerialName("daily")
    val daily: Daily
)