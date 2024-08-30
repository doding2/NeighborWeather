package map.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
actual fun GoogleMaps(
    modifier: Modifier,
    isControlsVisible: Boolean,
    onMarkerClick: ((MapMarker) -> Unit)?,
    onMapClick: ((LatLong) -> Unit)?,
    onMapLongClick: ((LatLong) -> Unit)?,
    markers: List<MapMarker>?,
    cameraPosition: CameraPosition?,
    cameraPositionLatLongBounds: CameraPositionLatLongBounds?,
    polyLine: List<LatLong>?
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(cameraPosition) {
        cameraPosition?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        it.target.latitude,
                        it.target.longitude
                    ), it.zoom
                )
            )
        }
    }

    LaunchedEffect(cameraPositionLatLongBounds) {
        cameraPositionLatLongBounds?.let {

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

    GoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier
    ) {
        markers?.forEach { marker ->
            Marker(
                state = rememberMarkerState(
                    key = marker.key,
                    position = LatLng(marker.position.latitude, marker.position.longitude)
                ),
                alpha = marker.alpha,
                title = marker.title
            )
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