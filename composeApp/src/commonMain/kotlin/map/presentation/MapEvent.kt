package map.presentation

import map.domain.model.Location
import map.domain.model.MapMarker

sealed interface MapEvent {
    data object NavigateUp: MapEvent
    data object AcceptedLocationPermission: MapEvent
    data object DeniedLocationPermission: MapEvent
    data class OnMapClick(val location: Location): MapEvent
    data class OnMarkerClick(val marker: MapMarker): MapEvent
    data class OnMyLocationClick(val location: Location): MapEvent
}