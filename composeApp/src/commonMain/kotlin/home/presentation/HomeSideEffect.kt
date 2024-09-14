package home.presentation

import weather.domain.model.WeatherType

sealed interface HomeSideEffect {
    data class NavigateToMap(val weatherType: WeatherType): HomeSideEffect
    data object OpenPermissionSettingPage: HomeSideEffect
    data class ShowSnackbar(val message: String): HomeSideEffect
}