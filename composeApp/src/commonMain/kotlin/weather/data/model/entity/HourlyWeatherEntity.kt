package weather.data.model.entity

import androidx.room.Entity
import weather.domain.model.Neighbor

@Entity(primaryKeys = ["locationName", "neighbor", "epochTime"])
data class HourlyWeatherEntity(
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val neighbor: Neighbor,
    val epochTime: Long,
    val temperature: Double,
    val relativeHumidity: Double,
    val precipitation: Double,
    val precipitationProbability: Double?,
    val weatherCode: Int,
    val windSpeed: Double,
    val windDirection: Double
)
