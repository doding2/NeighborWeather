package map.presentation

sealed interface MapSideEffect {
    data object OpenPermissionSettingPage: MapSideEffect
}