package home.presentation

sealed interface HomeSideEffect {
    data class ShowSnackbar(val message: String, val isImmediate: Boolean = false): HomeSideEffect
}