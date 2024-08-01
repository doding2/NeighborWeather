package home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import core.util.onError
import core.util.onSuccess
import kotlinx.coroutines.launch
import weather.domain.model.Neighbor
import weather.domain.repository.WeatherRepository

class HomeViewModel(
    private val weatherRepository: WeatherRepository
): ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    init {
        viewModelScope.launch {
            weatherRepository.getWeather(
                latitude = 36.0,
                longitude = 127.0,
                locationName = "서울",
                targetToWeight = mapOf(
                    Neighbor.Korea to 0.5,
                    Neighbor.Japan to 0.2,
                    Neighbor.China to 0.2,
                    Neighbor.USA to 0.1
                )
            ).onSuccess {
                state = state.copy(weather = it)
                Logger.d("weather: $it")
            }
            .onError {
                state = state.copy(error = it)
                Logger.e(it.toString())
            }
        }
    }

    fun onEvent(event: HomeEvent) {

    }
}