package home.presentation

import androidx.compose.runtime.Stable
import dev.jordond.compass.Place
import map.domain.model.Location
import org.jetbrains.compose.resources.DrawableResource
import weather.domain.model.Weather

@Stable
data class HomeState(
    val weather: Weather? = null,
    val myLocation: Location? = null,
    val myPlace: Place? = null,
    val backgroundImage: DrawableResource? = null,
)
