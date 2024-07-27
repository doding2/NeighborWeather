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
    val currentUnits: NeighborCurrentUnits,
    @SerialName("current")
    val current: NeighborCurrent,
    @SerialName("hourly_units")
    val hourlyUnits: NeighborHourlyUnits,
    @SerialName("hourly")
    val hourly: NeighborHourly,
    @SerialName("daily_units")
    val dailyUnits: NeighborDailyUnits,
    @SerialName("daily")
    val daily: NeighborDaily
)