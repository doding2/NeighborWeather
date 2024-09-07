package home.presentation

sealed interface HomeSideEffect {
    data object NavigateToMap: HomeSideEffect
    data object OpenPermissionSettingPage: HomeSideEffect
    data class ShowSnackbar(val message: String): HomeSideEffect
}