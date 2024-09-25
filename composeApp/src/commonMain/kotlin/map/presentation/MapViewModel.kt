package map.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import core.domain.util.Result
import core.domain.util.onError
import core.domain.util.onSuccess
import core.presentation.util.SnackbarAction
import core.presentation.util.SnackbarEvent
import dev.jordond.compass.Place
import dev.jordond.compass.Priority
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.GeocoderResult
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.LocationRequest
import dev.jordond.compass.geolocation.TrackingStatus
import kotlinx.coroutines.Job
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
import weather.domain.model.Neighbor
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

    private var weatherJob: Job? = null

    init {
        loadMyLocation()

        snapshotFlow { state.myLocation }
            .onEach { location ->
                val place = location?.let { fetchPlace(it) }
                state = state.copy(myPlace = place)
            }.launchIn(viewModelScope)

        snapshotFlow { state.myPlace }
            .onEach { place ->
                if (place != null) {
                    updateWeather(place)
                } else {
                    state = state.copy(myWeather = null)
                }
            }.launchIn(viewModelScope)

        snapshotFlow { state.myWeather }
            .onEach { weather ->
                val updateWithMyWeather = state.run { selectedPlace != null && selectedPlace == myPlace }
                if (updateWithMyWeather) {
                    state = state.copy(selectedWeather = weather)
                }
            }.launchIn(viewModelScope)

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
            .onEach { selectedPlace ->
                // if myPlace is not loaded, set selectedWeather to null (=myWeather is null now).
                // selectedWeather will be updated at other snapshotFlow after myPlace is loaded.
                val updateWithMyWeather = state.run { myPlace == null || selectedPlace == myPlace }
                val weather = if (updateWithMyWeather) state.myWeather else selectedPlace?.let { fetchWeather(it) }
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
            is MapEvent.OnMapClick -> {
                state = state.copy(selectedLocation = event.location)
            }
            is MapEvent.OnMarkerClick -> {
                updateSelectedLocation(event.marker.position)
            }
            is MapEvent.OnMyLocationClick -> {
                updateSelectedLocation(state.myLocation ?: event.location)
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
                                priority = Priority.HighAccuracy,
                                interval = 5000L,  // 5 seconds
                            )
                        )
                        logger.d("[Success] Start tracking")
                    }
                    TrackingStatus.Tracking -> {}
                    is TrackingStatus.Update -> {
                        val location = status.location.coordinates.let {
                            Location(it.latitude, it.longitude)
                        }
                        if (state.myPlace == null) {
                            state = state.run {
                                copy(
                                    myLocation = location,
                                    selectedLocation = selectedLocation ?: location
                                )
                            }
                            logger.d("[Success] update location: ${status.location}")
                        }
                    }
                    is TrackingStatus.Error -> {
                        val isPermissionDenied = status.cause is GeolocatorResult.PermissionDenied
                                || status.cause is GeolocatorResult.PermissionError
                        val snackbarEvent = if (isPermissionDenied) {
                            SnackbarEvent(
                                message = "Location permission is denied",
                                action = SnackbarAction(
                                    name = "Open setting",
                                    action = { sendEffect(MapSideEffect.OpenPermissionSettingPage) }
                                )
                            )
                        } else {
                            SnackbarEvent(status.cause.message)
                        }

                        sendEffect(MapSideEffect.ShowSnackbar(snackbarEvent))
                        logger.e("[Error] Fail to track location: ${status.cause}")
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
                    sendEffect(MapSideEffect.ShowSnackbar(
                        SnackbarEvent(message = "Fail to load place info")
                    ))
                }
                logger.e("[Error] Fail to load place info: ${result.errorOrNull()}")
                null
            }
        }
    }

    private suspend fun fetchWeather(place: Place): Weather? {
        return when (val result = weatherRepository.fetchRemoteWeather(place)) {
            is Result.Success -> result.data
            is Result.Error -> {
                sendEffect(MapSideEffect.ShowSnackbar(
                    SnackbarEvent(message = result.error.toString())
                ))
                logger.e("[Error] Fail to load weather: ${result.error}")
                null
            }
        }
    }

    private fun updateWeather(place: Place) {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            weatherRepository.loadWeathers(
                place = place,
                neighborWeights = mapOf(
                    Neighbor.Korea to 0.5,
                    Neighbor.Japan to 0.2,
                    Neighbor.China to 0.2,
                    Neighbor.USA to 0.1
                ),
                fetchFromRemote = false
            ).collect { result ->
                result
                    .onSuccess {
                        state = state.copy(myWeather = it)
                    }
                    .onError {
                        logger.e("[Error] $it")
                        sendEffect(MapSideEffect.ShowSnackbar(
                            SnackbarEvent(message = it.toString())
                        ))
                    }
            }
        }
    }

    private fun updateSelectedLocation(
        location: Location
    ) {
        /* When marker is double-clicked with term, state is not updated.
        Because location of marker is same value.
        So update camera position directly
         */
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