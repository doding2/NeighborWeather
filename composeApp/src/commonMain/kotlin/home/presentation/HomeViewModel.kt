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
import dev.jordond.compass.geolocation.LocationRequest
import dev.jordond.compass.geolocation.TrackingStatus
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
    private val weatherRepository: WeatherRepository,
    private val geolocator: Geolocator
): ViewModel() {

    private val logger by lazy { Logger.withTag("HomeViewModel") }

    var state by mutableStateOf(HomeState())
        private set

    private val _effect = Channel<HomeSideEffect>()
    val effect = _effect.receiveAsFlow()

    private var weatherJob: Job? = null

    init {
        snapshotFlow { state.myLocation }
            .onEach { location ->
                state = state.copy(
                    myPlace = location?.let { fetchPlace(it) }
                )
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
            HomeEvent.NavigateToMap -> {
                viewModelScope.launch {
                    sendEffect(HomeSideEffect.NavigateToMap)
                }
            }
            HomeEvent.AcceptedLocationPermission -> loadMyLocation()
            HomeEvent.DeniedLocationPermission -> {
                // TODO: Show dialog or etc
                viewModelScope.launch {
                    sendEffect(HomeSideEffect.OpenPermissionSettingPage)
                }
            }
        }
    }

    private fun loadMyLocation() {
        viewModelScope.launch {
            geolocator.trackingStatus.collect { status ->
                when (status) {
                    TrackingStatus.Idle -> {
                        geolocator.startTracking(
                            request = LocationRequest(
                                interval = 60000L,  // 1 minute
                            )
                        )
                        logger.d("Start tracking")
                    }
                    TrackingStatus.Tracking -> {
                        logger.d("Tracking")
                    }
                    is TrackingStatus.Update -> {
                        val location = status.location.coordinates.let {
                            Location(it.latitude, it.longitude)
                        }
                        if (state.myLocation == null) {
                            state = state.copy(
                                myLocation = location
                            )
                            logger.d("Update ${status.location}")
                        }
                    }
                    is TrackingStatus.Error -> {
                        logger.d("Error ${status.cause}")
                        state = state.copy(myLocation = null)
                        sendEffect(HomeSideEffect.ShowSnackbar(status.cause.message))
                    }
                }
            }
        }
    }

    private suspend fun fetchPlace(location: Location): Place? {
        val geocoder = Geocoder()
        val result = geocoder.places(
            latitude = location.latitude,
            longitude = location.longitude
        )
        return when (result) {
            is GeocoderResult.Success -> {
                logger.d("Success to load place: ${result.data}")
                result.getFirstOrNull()
            }
            is GeocoderResult.Error -> {
                sendEffect(HomeSideEffect.ShowSnackbar("Fail to load place info."))
                logger.d("Fail to load place info.")
                null
            }
        }
    }

    private fun updateWeather(place: Place) {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
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