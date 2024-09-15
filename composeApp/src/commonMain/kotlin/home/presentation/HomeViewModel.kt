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
import neighborweather.composeapp.generated.resources.background_weather_cloudy
import neighborweather.composeapp.generated.resources.background_weather_rainy
import neighborweather.composeapp.generated.resources.background_weather_snowy
import neighborweather.composeapp.generated.resources.background_weather_sunny
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
        initBackgroundImage()

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
                    state = state.copy(weather = null)
                }
            }.launchIn(viewModelScope)

        snapshotFlow { state.weather?.current?.weatherType }
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
            HomeEvent.AcceptedLocationPermission -> loadMyLocation()
            HomeEvent.DeniedLocationPermission -> {
                // TODO: Show dialog or etc
                viewModelScope.launch { sendEffect(HomeSideEffect.OpenPermissionSettingPage) }
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
                            state = state.copy(myLocation = location)
                            logger.d("[Success] Update ${status.location}")
                        }
                    }
                    is TrackingStatus.Error -> {
                        logger.d("[Error] Fail to track location: ${status.cause}")
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
            is GeocoderResult.Success -> result.data.getFirstDetailedPlace()
            is GeocoderResult.Error -> {
                sendEffect(HomeSideEffect.ShowSnackbar("Fail to load place info"))
                logger.d("[Error] Fail to load place info: ${result.errorOrNull()}")
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
                )
            ).collect { result ->
                result
                    .onSuccess {
                        state = state.copy(weather = it)
                    }
                    .onError {
                        logger.e("[Error] $it")
                        sendEffect(
                            HomeSideEffect.ShowSnackbar(it.toString())
                        )
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
            WeatherType.CloudySunny,
            WeatherType.Sunny -> Res.drawable.background_weather_sunny
            WeatherType.Other -> null
        }
    }
}