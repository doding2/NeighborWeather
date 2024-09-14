package map.presentation

sealed interface MapSideEffect {
    data object NavigateUp: MapSideEffect
    data object OpenPermissionSettingPage: MapSideEffect
    data class ShowSnackbar(val message: String): MapSideEffect
}