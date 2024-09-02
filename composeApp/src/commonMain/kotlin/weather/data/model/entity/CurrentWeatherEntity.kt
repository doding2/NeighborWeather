package weather.data.model.entity

import androidx.room.Entity
import weather.domain.model.Neighbor

@Entity(primaryKeys = ["latitude", "longitude", "neighbor", "epochTime"])
data class CurrentWeatherEntity(
    val latitude: Double,
    val longitude: Double,
    val neighbor: Neighbor,
    val epochTime: Long,
    val temperature: Double,
    val relativeHumidity: Double,
    val apparentTemperature: Double,
    val precipitation: Double,
    val precipitationProbability: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val windDirection: Double
)
