package map.presentation

sealed interface MapEvent {
    data object DeniedLocationPermission: MapEvent
    data object NavigateUp: MapEvent
}