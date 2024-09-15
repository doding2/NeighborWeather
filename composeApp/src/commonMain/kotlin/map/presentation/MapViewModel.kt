package map.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import core.domain.util.Result
import dev.jordond.compass.Place
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.GeocoderResult
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.LocationRequest
import dev.jordond.compass.geolocation.TrackingStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import map.domain.model.CameraPosition
import map.domain.model.Location
import map.domain.model.MapMarker
import map.domain.model.isWithinDistance
import map.domain.util.getFirstDetailedPlace
import weather.domain.model.Weather
import weather.domain.repository.WeatherRepository

class MapViewModel(
    private val weatherRepository: WeatherRepository,
    private val geolocator: Geolocator,
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

                val newMarker = location
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
                state = state.copy(selectedMarker = newMarker)

                // update selected place and camera position
                if (location == null) {
                    state = state.copy(selectedPlace = null)
                } else {
                    // Update state individually to trigger
                    // recomposition by camera position earlier.
                    state = state.copy(
                        cameraPosition = CameraPosition(
                            target = location
                        )
                    )
                    val place = fetchPlace(location)
                    state = state.copy(selectedPlace = place)
                }
            }.launchIn(viewModelScope)

        snapshotFlow { state.selectedPlace }
            .onEach { place ->
                // update weather of selected place
                val weather = place?.let { fetchWeather(it) }
                state = state.copy(
                    selectedWeather = weather
                )
            }.launchIn(viewModelScope)
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
            MapEvent.AcceptedLocationPermission -> loadMyLocation()
            MapEvent.DeniedLocationPermission -> {
                // TODO: Show dialog or etc
                viewModelScope.launch {
                    sendEffect(MapSideEffect.OpenPermissionSettingPage)
                }
            }
            is MapEvent.OnMapClick -> {
                state = state.copy(selectedLocation = event.location)
            }
            /* When marker is double-clicked with term, state is not updated.
            Because location of marker is same value.
            So update camera position directly
             */
            is MapEvent.OnMarkerClick -> {
                updateSelectedLocation(event.marker.position)
            }
            is MapEvent.OnMyLocationClick -> {
                updateSelectedLocation(event.location)
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
                    }
                    TrackingStatus.Tracking -> {}
                    is TrackingStatus.Update -> {
                        val location = status.location.coordinates.let {
                            Location(it.latitude, it.longitude)
                        }
                        if (state.myLocation == null) {
                            state = state.run {
                                copy(
                                    myLocation = location,
                                    selectedLocation = selectedLocation ?: location
                                )
                            }
                            logger.d("[Success] Update ${status.location}")
                        }
                    }
                    is TrackingStatus.Error -> {
                        logger.d("[Error] Error ${status.cause}")
                        state = state.copy(myLocation = null)
                        sendEffect(MapSideEffect.ShowSnackbar(status.cause.message))
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
            is GeocoderResult.Success -> result.data.getFirstDetailedPlace()
            is GeocoderResult.Error -> {
                if (result.errorOrNull() != GeocoderResult.NotFound) {
                    sendEffect(MapSideEffect.ShowSnackbar("Fail to load place info"))
                }
                logger.d("[Error] Fail to load place info: ${result.errorOrNull()}")
                null
            }
        }
    }

    private suspend fun fetchWeather(place: Place): Weather? {
        return when (val result = weatherRepository.fetchRemoteWeather(place)) {
            is Result.Success -> result.data
            is Result.Error -> {
                sendEffect(MapSideEffect.ShowSnackbar(result.error.toString()))
                logger.d("[Error] Fail to load weather: ${result.error}")
                null
            }
        }
    }

    private fun updateSelectedLocation(
        location: Location
    ) {
        val cameraOffset = (-5..5).random() / 10000000.0
        state = state.copy(
            selectedLocation = location,
            cameraPosition = CameraPosition(
                target = location.let {
                    Location(
                        latitude = it.latitude + cameraOffset,
                        longitude = it.longitude + cameraOffset
                    )
                }
            )
        )
    }
}