package home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.util.onError
import core.util.onSuccess
import kotlinx.coroutines.launch
import weather.domain.repository.WeatherRepository

class HomeViewModel(
    private val weatherRepository: WeatherRepository
): ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    init {
        viewModelScope.launch {
            weatherRepository.getWeather(36.0, 127.0, "서울")
                .onSuccess {
                    println(it.toString())
                    state = state.copy(weather = it)
                }
                .onError {
                    println(it.toString())
                }
        }
    }

    fun onEvent(event: HomeEvent) {

    }
}