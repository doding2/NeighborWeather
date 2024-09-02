package weather.data.model.entity

import androidx.room.Entity
import weather.domain.model.Neighbor

@Entity(primaryKeys = ["latitude", "longitude", "neighbor", "epochTime"])
data class DailyWeatherEntity(
    val latitude: Double,
    val longitude: Double,
    val neighbor: Neighbor,
    val epochTime: Long,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val precipitationProbability: Double?,
    val weatherCode: Int,
)
