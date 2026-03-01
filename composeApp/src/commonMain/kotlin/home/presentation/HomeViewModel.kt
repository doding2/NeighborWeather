package home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import map.domain.model.Location
import map.domain.util.getFirstDetailedPlace
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.background_fall_1
import neighborweather.composeapp.generated.resources.background_fall_2
import neighborweather.composeapp.generated.resources.background_fall_3
import neighborweather.composeapp.generated.resources.background_fall_4
import neighborweather.composeapp.generated.resources.background_spring_1
import neighborweather.composeapp.generated.resources.background_spring_2
import neighborweather.composeapp.generated.resources.background_spring_3
import neighborweather.composeapp.generated.resources.background_spring_4
import neighborweather.composeapp.generated.resources.background_summer_1
import neighborweather.composeapp.generated.resources.background_summer_2
import neighborweather.composeapp.generated.resources.background_summer_3
import neighborweather.composeapp.generated.resources.background_summer_4
import neighborweather.composeapp.generated.resources.background_weather_clear
import neighborweather.composeapp.generated.resources.background_weather_cloudy
import neighborweather.composeapp.generated.resources.background_weather_rainy
import neighborweather.composeapp.generated.resources.background_weather_snowy
import neighborweather.composeapp.generated.resources.background_winter_1
import neighborweather.composeapp.generated.resources.background_winter_2
import neighborweather.composeapp.generated.resources.background_winter_3
import neighborweather.composeapp.generated.resources.background_winter_4
import org.jetbrains.compose.resources.DrawableResource
import weather.domain.model.Neighbor
import weather.domain.model.WeatherType
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
        loadMyLocation()
        initBackgroundImage()

        snapshotFlow { state.myLocation }
            .onEach { location ->
                val place = location?.let { fetchPlace(it) }
                state = state.copy(myPlace = place)
            }.launchIn(viewModelScope)

        snapshotFlow { state.myPlace }
            .onEach { place ->
                if (state.neighborWeights == null) {
                    val neighborWeights = loadNeighborWeights()
                    state = state.copy(neighborWeights = neighborWeights)
                }

                val weights = state.neighborWeights
                if (place != null && weights != null) {
                    updateWeather(place, weights, true)
                } else {
                    state = state.copy(myWeather = null)
                }
            }.launchIn(viewModelScope)

        snapshotFlow { state.neighborWeights }
            // wait 300ms after last change before emitting
            // if another change happens within 300ms, the timer resets
            // this prevents heavy calculations from running too frequently during sliding
            .debounce(300)
            .onEach { weights ->
                val place = state.myPlace
                if (place != null && weights != null) {
                    // fetch weather data from remote only when myPlace is changed
                    // if only neighbor weights are changed, just use local data
                    updateWeather(place, weights, false)
                } else {
                    state = state.copy(myWeather = null)
                }
            }.launchIn(viewModelScope)

        snapshotFlow { state.myWeather?.current?.weatherType }
            .onEach { weatherType ->
                if (weatherType != null) {
                    val drawable = getBackgroundImage(weatherType)
                    drawable?.let {
                        state = state.copy(backgroundImage = it)
                    }
                }
            }.launchIn(viewModelScope)
    }

    private suspend fun sendEffect(effect: HomeSideEffect) {
        _effect.send(effect)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.NavigateToMap -> {
                viewModelScope.launch { sendEffect(HomeSideEffect.NavigateToMap) }
            }
            is HomeEvent.ToggleNavigationDrawer -> {
                viewModelScope.launch { sendEffect(HomeSideEffect.ToggleNavigationDrawer) }
            }
            is HomeEvent.OnClickNavigationItem -> {
//                updateWeather(event.item.first)
            }
            is HomeEvent.OnSlideNeighborWeight -> {
                updateNeighborWeights(event.item)
            }
        }
    }

    private suspend fun loadNeighborWeights(): Map<Neighbor, Double> {
        return weatherRepository.loadNeighborWeights()
    }

    private fun updateNeighborWeights(item: Pair<Neighbor, Double>) {
        viewModelScope.launch {
            val current = state.neighborWeights ?: return@launch
            val updated = current.toMutableMap()

            // update selected neighbor's weight
            val changedNeighbor = item.first
            val newWeight = item.second.coerceIn(0.0, 1.0)
            updated[changedNeighbor] = newWeight

            // get all other neighbors except changed neighbor
            val others = updated.filterKeys { it != changedNeighbor }
            val othersOldSum = others.values.sum()
            // remaining weight to distribute so total becomes 1.0
            val remain = (1.0 - newWeight).coerceAtLeast(0.0)

            if (othersOldSum > 0.0) {
                // scale other weights proportionally to keep their ratio
                val scale = remain / othersOldSum

                others.forEach { (neighbor, weight) ->
                    updated[neighbor] = weight * scale
                }
            } else {
                // if all other weights were 0, distribute equally
                val equalValue = if (others.isNotEmpty()) remain / others.size else 0.0
                others.keys.forEach { neighbor ->
                    updated[neighbor] = equalValue
                }
            }

            // save and update state
            weatherRepository.saveNeighborWeights(updated)
            state = state.copy(neighborWeights = updated)
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
                            state = state.copy(myLocation = location)
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
                                    action = { sendEffect(HomeSideEffect.OpenPermissionSettingPage) }
                                )
                            )
                        } else {
                            SnackbarEvent(message = status.cause.message)
                        }

                        sendEffect(HomeSideEffect.ShowSnackbar(snackbarEvent))
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
                sendEffect(HomeSideEffect.ShowSnackbar(
                    SnackbarEvent(message = "Fail to load place info")
                ))
                logger.e("[Error] Fail to load place info: ${result.errorOrNull()}")
                null
            }
        }
    }

    private fun updateWeather(
        place: Place,
        neighborWeight: Map<Neighbor, Double>,
        fetchFromRemote: Boolean
    ) {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            weatherRepository.loadWeathers(
                place = place,
                neighborWeights = neighborWeight,
                fetchFromRemote = fetchFromRemote
            ).collect { result ->
                result
                    .onSuccess {
                        state = state.copy(myWeather = it)
                        logger.d("[Success] $it")
                    }
                    .onError {
                        logger.e("[Error] $it")
                        sendEffect(HomeSideEffect.ShowSnackbar(
                            SnackbarEvent(message = it.toString())
                        ))
                    }
            }
        }
    }


    private fun initBackgroundImage() {
        val current = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val drawable = current.date.run {
            val randomNum = (1..4).random()
            val monthNum = month.ordinal + 1

            val isSpring = (monthNum == 3 && dayOfMonth >= 21) || (monthNum in 4..5) || (monthNum == 6 && dayOfMonth < 21)
            val isSummer = (monthNum == 6 && dayOfMonth >= 21) || (monthNum in 7..8) || (monthNum == 9 && dayOfMonth < 21)
            val isFall = (monthNum == 9 && dayOfMonth >= 21) || (monthNum in 10..11) || (monthNum == 12 && dayOfMonth < 21)
            val isWinter = (monthNum == 12 && dayOfMonth >= 21) || (monthNum in 1..2) || (monthNum == 3 && dayOfMonth < 21)

            when {
                isSpring -> when (randomNum) {
                    1 -> Res.drawable.background_spring_1
                    2 -> Res.drawable.background_spring_2
                    3 -> Res.drawable.background_spring_3
                    else -> Res.drawable.background_spring_4
                }
                isSummer -> when (randomNum) {
                    1 -> Res.drawable.background_summer_1
                    2 -> Res.drawable.background_summer_2
                    3 -> Res.drawable.background_summer_3
                    else -> Res.drawable.background_summer_4
                }
                isFall -> when (randomNum) {
                    1 -> Res.drawable.background_fall_1
                    2 -> Res.drawable.background_fall_2
                    3 -> Res.drawable.background_fall_3
                    else -> Res.drawable.background_fall_4
                }
                isWinter -> when (randomNum) {
                    1 -> Res.drawable.background_winter_1
                    2 -> Res.drawable.background_winter_2
                    3 -> Res.drawable.background_winter_3
                    else -> Res.drawable.background_winter_4
                }
                else -> when (randomNum) {
                    1 -> Res.drawable.background_winter_1
                    2 -> Res.drawable.background_winter_2
                    3 -> Res.drawable.background_winter_3
                    else -> Res.drawable.background_winter_4
                }
            }
        }
        state = state.copy(
            backgroundImage = drawable
        )
    }

    private fun getBackgroundImage(weatherType: WeatherType): DrawableResource? {
        return when (weatherType) {
            WeatherType.Fog,
            WeatherType.Cloudy -> Res.drawable.background_weather_cloudy
            WeatherType.Drizzle,
            WeatherType.Rainy,
            WeatherType.RainShower,
            WeatherType.Thunderstorm -> Res.drawable.background_weather_rainy
            WeatherType.Snowy,
            WeatherType.SnowShower,
            WeatherType.FreezingDrizzle,
            WeatherType.FreezingRain -> Res.drawable.background_weather_snowy
            WeatherType.MainlyClear,
            WeatherType.Clear -> Res.drawable.background_weather_clear
            WeatherType.Other -> null
        }
    }
}