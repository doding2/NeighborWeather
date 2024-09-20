package map.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSCameraUpdate
import cocoapods.GoogleMaps.GMSCameraUpdate.Companion.fitBounds
import cocoapods.GoogleMaps.GMSCoordinateBounds
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMapViewDelegateProtocol
import cocoapods.GoogleMaps.GMSMapViewOptions
import cocoapods.GoogleMaps.GMSMarker
import cocoapods.GoogleMaps.GMSMutablePath
import cocoapods.GoogleMaps.GMSPolyline
import cocoapods.GoogleMaps.animateWithCameraUpdate
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import map.domain.model.CameraPosition
import map.domain.model.CameraPositionLocationBounds
import map.domain.model.Location
import map.domain.model.MapMarker
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun GoogleMaps(
    modifier: Modifier,
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
) {
    val mapsViewDelegate = remember {
        object : NSObject(), GMSMapViewDelegateProtocol {
            override fun mapView(
                mapView: GMSMapView,
                didTapAtCoordinate: CValue<CLLocationCoordinate2D>,
            ) {
                didTapAtCoordinate.useContents {
                    onMapClick?.invoke(
                        Location(
                            latitude = this.latitude,
                            longitude = this.longitude
                        )
                    )
                }
            }
        }
    }
    val mapsView = remember {
        val options = GMSMapViewOptions().apply {
            camera = GMSCameraPosition.cameraWithLatitude(
                latitude = 51.5,
                longitude = -0.12,
                zoom = 10f
            )
        }
        GMSMapView(options = options).apply {
            myLocationEnabled = true
        }
    }

    UIKitView(
        factory = {
            mapsView
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            view.settings.apply {
                compassButton = isControlsVisible
                myLocationButton = isControlsVisible
            }
            view.setDelegate(mapsViewDelegate)
            view.clear()

            if (cameraPosition != null) {
                view.animateWithCameraUpdate(
                    GMSCameraUpdate.setCamera(
                        GMSCameraPosition.cameraWithLatitude(
                            cameraPosition.target.latitude,
                            cameraPosition.target.longitude,
                            cameraPosition.zoom ?: view.camera.zoom
                        )
                    )
                )
            }

            cameraPositionLocationBounds?.let {
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
        },
        properties = UIKitInteropProperties(
            isNativeAccessibilityEnabled = true,
            interactionMode = UIKitInteropInteractionMode.NonCooperative
        )
    )
}