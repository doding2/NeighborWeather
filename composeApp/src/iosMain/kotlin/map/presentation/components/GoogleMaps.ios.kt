package map.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSCameraUpdate
import cocoapods.GoogleMaps.GMSCameraUpdate.Companion.fitBounds
import cocoapods.GoogleMaps.GMSCoordinateBounds
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMarker
import cocoapods.GoogleMaps.GMSMutablePath
import cocoapods.GoogleMaps.GMSPolyline
import cocoapods.GoogleMaps.animateWithCameraUpdate
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake

@OptIn(ExperimentalForeignApi::class)
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
    val mapsView = remember {
        GMSMapView()
    }

    UIKitView(
        modifier = modifier.fillMaxSize(),
        interactive = true,
        factory = {
            mapsView
        },
        update = { view ->
            cameraPosition?.let {
                view.setCamera(
                    GMSCameraPosition.cameraWithLatitude(
                        it.target.latitude,
                        it.target.longitude,
                        it.zoom
                    )
                )
            }

            cameraPositionLatLongBounds?.let {
                val bounds = GMSCoordinateBounds()
                it.coordinates.forEach {
                    bounds.includingCoordinate(
                        CLLocationCoordinate2DMake(
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    )
                }
                GMSCameraUpdate().apply {
                    fitBounds(bounds, it.padding.toDouble())
                    view.animateWithCameraUpdate(this)
                }
            }

            markers?.forEach { marker ->
                GMSMarker().apply {
                    position = CLLocationCoordinate2DMake(
                        marker.position.latitude,
                        marker.position.longitude
                    )
                    title = marker.title
                    map = view
                }
            }

            polyLine?.let { polyLine ->
                val points = polyLine.map {
                    CLLocationCoordinate2DMake(it.latitude, it.longitude)
                }
                val path = GMSMutablePath().apply {
                    points.forEach { point ->
                        addCoordinate(point)
                    }
                }

                GMSPolyline().apply {
                    this.path = path
                    this.map = view
                }
            }
        }
    )
}