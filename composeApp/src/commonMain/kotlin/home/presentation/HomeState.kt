package home.presentation

import androidx.compose.runtime.Stable
import weather.domain.model.Weather

@Stable
data class HomeState(
    val weather: Weather? = null
)
