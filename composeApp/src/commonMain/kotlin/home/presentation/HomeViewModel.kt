package home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import core.util.onError
import core.util.onSuccess
import dev.jordond.compass.Place
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.GeocoderResult
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.mobile
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import map.domain.model.Location
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

    private var job: Job? = null

    init {
        snapshotFlow { state.myLocation }
            .onEach { location ->
                if (location != null) {
                    loadPlace(location)
                } else {
                    state = state.copy(myPlace = null)
                }
            }.launchIn(viewModelScope)

        snapshotFlow { state.myPlace }
            .onEach { place ->
                if (place != null) {
                    updateWeather(place)
                } else {
                    state = state.copy(weather = null)
                }
            }.launchIn(viewModelScope)
    }

    private suspend fun sendEffect(effect: HomeSideEffect) {
        _effect.send(effect)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.NavigateToMap -> {
                viewModelScope.launch {
                    sendEffect(HomeSideEffect.NavigateToMap)
                }
            }
            is HomeEvent.AcceptedLocationPermission -> loadMyLocation()
            is HomeEvent.DeniedLocationPermission -> {
                // TODO: Show dialog or etc
                viewModelScope.launch {
                    sendEffect(HomeSideEffect.OpenPermissionSettingPage)
                }
            }
        }
    }

    private fun loadMyLocation() {
        viewModelScope.launch {
            val geolocator = Geolocator.mobile()
            when (val result = geolocator.current()) {
                is GeolocatorResult.Success -> {
                    val location = result.data.coordinates.let {
                        Location(it.latitude, it.longitude)
                    }
                    state = state.copy(myLocation = location,)
                    logger.d("Success to load my location: ${result.data}")
                }
                is GeolocatorResult.Error -> {
                    state = state.copy(myLocation = null)
                    sendEffect(HomeSideEffect.ShowSnackbar(result.message))
                    logger.d("Fail to load my location: ${result.message}")
                }
            }
        }
    }

    private suspend fun loadPlace(location: Location) {
        val geocoder = Geocoder()
        val result = geocoder.places(
            latitude = location.latitude,
            longitude = location.longitude
        )
        when (result) {
            is GeocoderResult.Success -> {
                state = state.copy(myPlace = result.getFirstOrNull())
                logger.d("Success to load place: ${result.data}")
            }
            is GeocoderResult.Error -> {
                state = state.copy(myPlace = null)
                sendEffect(HomeSideEffect.ShowSnackbar("Fail to load place info."))
                logger.d("Fail to load place info.")
            }
        }
    }

    private fun updateWeather(place: Place) {

        job?.cancel()
        job = viewModelScope.launch {
            weatherRepository.loadWeathers(
                place = place,
                targetToWeight = mapOf(
                    Neighbor.Korea to 0.5,
                    Neighbor.Japan to 0.2,
                    Neighbor.China to 0.2,
                    Neighbor.USA to 0.1
                )
            ).collect { result ->
                result
                    .onSuccess {
                        logger.d {
                            """
                                [UI] ${it.neighbor}
                                    current: ${it.current.time}
                                    hourly: ${it.hourly.map { it.time }}
                                    daily: ${it.daily.map { it.time }}
                            """.trimIndent()
                        }

                        state = state.copy(weather = it)
                    }
                    .onError {
                        logger.e("[error] $it")
                        sendEffect(
                            HomeSideEffect.ShowSnackbar(it.toString())
                        )
                    }
            }
        }
    }
}