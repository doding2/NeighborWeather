package map.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import map.domain.model.CameraPosition
import map.domain.model.CameraPositionLocationBounds
import map.domain.model.Location
import map.domain.model.MapMarker

@Composable
expect fun GoogleMaps(
    modifier: Modifier,
    isControlsVisible: Boolean = true,
    onMarkerClick: ((MapMarker) -> Unit)? = {},
    onMapClick: ((Location) -> Unit)? = {},
    onMapLongClick: ((Location) -> Unit)? = {},
    onMyLocationClick: ((Location) -> Unit)? = {},
    markers: List<MapMarker>? = null,
    cameraPosition: CameraPosition?,
    cameraPositionLocationBounds: CameraPositionLocationBounds? = null,
    polyLine: List<Location>? = null,
    contentPadding: PaddingValues = PaddingValues(),
)
