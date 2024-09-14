package weather.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime

@Immutable
data class HourlyWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val relativeHumidity: Double,
    val precipitation: Double,
    val precipitationProbability: Double,
    val weatherCode: Int,
    val weatherType: WeatherType,
    val windSpeed: Double,
    val windDirection: Double
)
