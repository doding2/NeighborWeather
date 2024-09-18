package home.presentation

import core.presentation.util.SnackbarEvent

sealed interface HomeSideEffect {
    data object NavigateToMap: HomeSideEffect
    data object OpenPermissionSettingPage: HomeSideEffect
    data class ShowSnackbar(val event: SnackbarEvent): HomeSideEffect
}