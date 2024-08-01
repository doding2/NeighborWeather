package home.presentation

import androidx.compose.runtime.Stable
import core.util.Error
import weather.domain.model.Weather

@Stable
data class HomeState(
    val weather: Weather? = null,
    val error: Error? = null
)
