package weather.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime

@Immutable
data class CurrentWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val relativeHumidity: Double,
    val apparentTemperature: Double,
    val precipitation: Double,
    val precipitationProbability: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val windDirection: Double
)
