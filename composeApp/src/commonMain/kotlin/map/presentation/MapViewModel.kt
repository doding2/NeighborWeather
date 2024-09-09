package map.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import core.util.Result
import dev.jordond.compass.Place
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.GeocoderResult
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.mobile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import map.domain.model.CameraPosition
import map.domain.model.Location
import map.domain.model.MapMarker
import map.domain.model.isWithinDistance
import weather.domain.model.Weather
import weather.domain.repository.WeatherRepository

class MapViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val logger by lazy { Logger.withTag("MapViewModel") }

    var state by mutableStateOf(MapState())
        private set

    private val _effect: Channel<MapSideEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    init {
        snapshotFlow { state.selectedLocation }
            .onEach { location ->
                // update selected marker
                val isWithinMe = location?.let {
                    state.myLocation?.isWithinDistance(it, 0.005)
                } ?: false

                state = state.copy(
                    selectedMarker = location
                        ?.takeIf { !isWithinMe }
                        ?.let {
                            val key = "(${location.latitude}, ${location.longitude})"
                            MapMarker(
                                key = key,
                                position = Location(
                                    location.latitude,
                                    location.longitude
                                ),
                                title = key,
                                alpha = 1f,
                            )
                        }
                )

                // update selected place and camera position
                if (location == null) {
                    state = state.copy(
                        selectedPlace = null
                    )
                } else {
                    state = state.copy(
                        cameraPosition = CameraPosition(
                            target = location
                        )
                    )
                    loadPlace(location)
                }
            }.launchIn(viewModelScope)

        snapshotFlow { state.selectedPlace }
            .onEach { place ->
                // update weather of selected place
                state = state.copy(
                    selectedWeather = place?.let { fetchWeather(it) }
                )
            }.launchIn(viewModelScope)
    }

    private suspend fun sendEffect(effect: MapSideEffect) {
        _effect.send(effect)
    }

    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.NavigateUp -> {
                viewModelScope.launch {
                    sendEffect(MapSideEffect.NavigateUp)
                }
            }
            is MapEvent.AcceptedLocationPermission -> loadMyLocation()
            is MapEvent.DeniedLocationPermission -> {
                // TODO: Show dialog or etc
                viewModelScope.launch {
                    sendEffect(MapSideEffect.OpenPermissionSettingPage)
                }
            }
            is MapEvent.OnMapClick -> {
                updateSelectedLocation(location = event.location)
            }
            /* When marker is double-clicked with term, state is not updated.
            Because location of marker is same value.
            So update camera position directly
             */
            is MapEvent.OnMarkerClick -> {
                updateSelectedLocation(
                    location = event.marker.position,
                    updateCameraPosition = true
                )
            }
            is MapEvent.OnMyLocationClick -> {
                updateSelectedLocation(
                    location = event.location,
                    updateCameraPosition = true
                )
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
                    state = state.copy(
                        myLocation = location,
                        selectedLocation = location,
                    )
                    logger.d("Success to load my location: ${result.data}")
                }
                is GeolocatorResult.Error -> {
                    state = state.copy(myLocation = null)
                    sendEffect(MapSideEffect.ShowSnackbar(result.message))
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
                state = state.copy(
                    selectedPlace = result.getFirstOrNull()
                )
                logger.d("Success to load place: ${result.data}")
            }
            is GeocoderResult.Error -> {
                state = state.copy(
                    selectedPlace = null
                )
                if (result.errorOrNull() != GeocoderResult.NotFound) {
                    sendEffect(MapSideEffect.ShowSnackbar("Fail to load place info: ${result.errorOrNull()?.toString()}"))
                }
                logger.d("Fail to load place info.")
            }
        }
    }

    private fun updateSelectedLocation(
        location: Location,
        updateCameraPosition: Boolean = false
    ) {
        val cameraOffset = (-5..5).random() / 10000000.0
        state = state.copy(
            selectedLocation = location,
            cameraPosition = if (!updateCameraPosition) state.cameraPosition
            else {
                CameraPosition(
                    target = location.let {
                        Location(
                            latitude = it.latitude + cameraOffset,
                            longitude = it.longitude + cameraOffset
                        )
                    }
                )
            }
        )
    }

    private suspend fun fetchWeather(place: Place): Weather? {
        return when (val result = weatherRepository.fetchWeather(place)) {
            is Result.Success -> {
                val weather = result.data
                logger.d("Success to load weather: $weather")
                weather
            }
            is Result.Error -> {
                sendEffect(MapSideEffect.ShowSnackbar(result.error.toString()))
                logger.d("Fail to load weather: ${result.error}")
                null
            }
        }
    }
}