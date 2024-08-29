package home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import core.util.onError
import core.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import weather.domain.model.Neighbor
import weather.domain.repository.WeatherRepository

class HomeViewModel(
    private val weatherRepository: WeatherRepository
): ViewModel() {

    private val logger by lazy { Logger.withTag("HomeViewModel") }

    var state by mutableStateOf(HomeState())
        private set

    private val _effect = Channel<HomeSideEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            weatherRepository.getWeathers(
                latitude = 36.0,
                longitude = 127.0,
                locationName = "서울",
                targetToWeight = mapOf(
                    Neighbor.Korea to 0.5,
                    Neighbor.Japan to 0.2,
                    Neighbor.China to 0.2,
                    Neighbor.USA to 0.1
                )
            ).collect { result ->
                result.onSuccess {
                    logger.d {
                        """
                            [UI] ${it.neighbor}
                                current: ${it.current.time}
                                hourly: ${it.hourly.map { it.time }}
                                daily: ${it.daily.map { it.time }}
                        """.trimIndent()
                    }

                    state = state.copy(weather = it)
                    sendEffect(HomeSideEffect.ShowSnackbar("Update weather"))
                }
                .onError {
                    logger.e("[error] $it")
                    sendEffect(HomeSideEffect.ShowSnackbar(it.toString()))
                }
            }
        }
    }

    fun onEvent(event: HomeEvent) {

    }

    private suspend fun sendEffect(effect: HomeSideEffect) {
        _effect.send(effect)
    }
}