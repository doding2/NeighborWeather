package home.presentation

import dev.jordond.compass.Place
import weather.domain.model.Neighbor
import weather.domain.model.Weather

sealed interface HomeEvent {
    data object NavigateToMap: HomeEvent
    data object ToggleNavigationDrawer: HomeEvent
    data class OnClickNavigationItem(val item: Pair<Place, Weather>): HomeEvent
    data class OnSlideNeighborWeight(val item: Pair<Neighbor, Double>): HomeEvent
}