package map.presentation

import androidx.compose.runtime.Stable
import dev.jordond.compass.Place
import map.domain.model.CameraPosition
import map.domain.model.Location
import map.domain.model.MapMarker

@Stable
data class MapState(
    val myLocation: Location? = null,
    val selectedLocation: Location? = null,
    val selectedPlace: Place? = null,
    val selectedMarker: MapMarker? = null,
    val cameraPosition: CameraPosition? = null,
    val markers: List<MapMarker> = emptyList()
)
