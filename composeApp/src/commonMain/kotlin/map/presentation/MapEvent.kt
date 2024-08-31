package map.presentation

sealed interface MapEvent {
    data object DeniedAlwaysLocationPermission: MapEvent
    data object DeniedLocationPermission: MapEvent
    data object CanceledLocationPermission: MapEvent
}