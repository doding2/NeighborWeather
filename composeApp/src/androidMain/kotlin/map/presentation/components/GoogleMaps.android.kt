package map.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import map.domain.model.CameraPosition
import map.domain.model.CameraPositionLocationBounds
import map.domain.model.Location
import map.domain.model.MapMarker

@Composable
actual fun GoogleMaps(
    isControlsVisible: Boolean,
    onMarkerClick: ((MapMarker) -> Unit)?,
    onMapClick: ((Location) -> Unit)?,
    onMapLongClick: ((Location) -> Unit)?,
    onMyLocationClick: ((Location) -> Unit)?,
    markers: List<MapMarker>?,
    cameraPosition: CameraPosition?,
    cameraPositionLocationBounds: CameraPositionLocationBounds?,
    polyLine: List<Location>?,
    contentPadding: PaddingValues,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState() {
        position = com.google.android.gms.maps.model.CameraPosition(
            LatLng(0.0, 0.0), 10f, 0f, 0f
        )
    }
    LaunchedEffect(cameraPosition) {
        cameraPosition?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.target.latitude, it.target.longitude),
                    cameraPosition.zoom ?: cameraPositionState.position.zoom
                )
            )
        }
    }
    LaunchedEffect(cameraPositionLocationBounds) {
        cameraPositionLocationBounds?.let {
            val latLngBounds = LatLngBounds.builder().apply {
                it.coordinates.forEach { latLong ->
                    include(LatLng(latLong.latitude, latLong.longitude))
                }
            }.build()

            cameraPositionState.move(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, it.padding)
            )
        }
    }
    val uiSettings by remember(isControlsVisible) { mutableStateOf(MapUiSettings(
        compassEnabled = isControlsVisible,
        myLocationButtonEnabled = isControlsVisible,
        zoomControlsEnabled = isControlsVisible
    )) }
    val properties by remember { mutableStateOf(MapProperties(
        isMyLocationEnabled = true
    )) }
    GoogleMap(
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties,
        onMapClick = { onMapClick?.invoke(Location(it.latitude, it.longitude)) },
        onMyLocationClick = { onMyLocationClick?.invoke(Location(it.latitude, it.longitude)) },
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        markers?.forEach { marker ->
            key(marker.key) {
                Marker(
                    state = rememberMarkerState(
                        key = marker.key,
                        position = LatLng(marker.position.latitude, marker.position.longitude)
                    ),
                    alpha = marker.alpha,
                    title = marker.title,
                    onClick = {
                        onMarkerClick?.invoke(marker)
                        true
                    }
                )
            }
        }

        polyLine?.let { polyLine ->
            Polyline(
                points = List(polyLine.size) {
                    val latLong = polyLine[it]
                    LatLng(latLong.latitude, latLong.longitude)
                },
                color = Color(0XFF1572D5),
                width = 16f
            )
            Polyline(
                points = List(polyLine.size) {
                    val latLong = polyLine[it]
                    LatLng(latLong.latitude, latLong.longitude)
                },
                color = Color(0XFF00AFFE),
                width = 8f
            )
        }

    }
}