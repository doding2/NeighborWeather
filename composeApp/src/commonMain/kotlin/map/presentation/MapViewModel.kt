package map.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    var state by mutableStateOf(MapState())
        private set

    private val _effect: Channel<MapSideEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    init {

    }

    private suspend fun sendEffect(effect: MapSideEffect) {
        _effect.send(effect)
    }

    fun onEvent(event: MapEvent) {
        when (event) {
            MapEvent.NavigateUp -> {
                viewModelScope.launch {
                    sendEffect(MapSideEffect.NavigateUp)
                }
            }
            MapEvent.DeniedLocationPermission -> {
                // TODO: Show dialog or etc
                viewModelScope.launch {
                    sendEffect(MapSideEffect.OpenPermissionSettingPage)
                }
            }
        }
    }

}