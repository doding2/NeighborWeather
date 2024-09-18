package map.presentation

import core.presentation.util.SnackbarEvent

sealed interface MapSideEffect {
    data object NavigateUp: MapSideEffect
    data object OpenPermissionSettingPage: MapSideEffect
    data class ShowSnackbar(val event: SnackbarEvent): MapSideEffect
}