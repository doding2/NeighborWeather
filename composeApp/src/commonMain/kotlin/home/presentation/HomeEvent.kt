package home.presentation

sealed interface HomeEvent {
    data object NavigateToMap: HomeEvent
}