package weather.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class DailyWeather(
    val time: LocalDate,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val precipitationProbability: Double,
    val weatherCode: Int,
    val weatherType: WeatherType,
)
