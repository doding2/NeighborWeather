package home.presentation

import weather.domain.model.WeatherType

sealed interface HomeEvent {
    data class NavigateToMap(val weatherType: WeatherType): HomeEvent
    data object AcceptedLocationPermission: HomeEvent
    data object DeniedLocationPermission: HomeEvent
}