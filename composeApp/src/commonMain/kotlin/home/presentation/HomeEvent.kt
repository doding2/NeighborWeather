package home.presentation

sealed interface HomeEvent {
    data object NavigateToMap: HomeEvent
    data object AcceptedLocationPermission: HomeEvent
    data object DeniedLocationPermission: HomeEvent
}