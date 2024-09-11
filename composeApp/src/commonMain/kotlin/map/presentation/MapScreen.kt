package map.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import core.presentation.util.EdgeColors
import core.presentation.util.ObserveEffectsOnLifecycle
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import map.presentation.components.GoogleMaps
import map.presentation.components.MapPlaceWeather
import map.presentation.components.MapSearchBar

@Composable
fun MapScreen(
    state: MapState,
    onEvent: (MapEvent) -> Unit,
    effect: Flow<MapSideEffect>,
    navController: NavController
) {
    EdgeColors(
        navBarColor = Color.Transparent,
    )
    val permissionsControllerFactory = rememberPermissionsControllerFactory()
    val permissionsController = remember(permissionsControllerFactory) {
        permissionsControllerFactory.createPermissionsController()
    }
    val scope = rememberCoroutineScope()
    BindEffect(permissionsController)
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                permissionsController.providePermission(Permission.LOCATION)
                onEvent(MapEvent.AcceptedLocationPermission)
            } catch (e: Throwable) {
                when (e) {
                    is DeniedAlwaysException,
                    is DeniedException,
                    is RequestCanceledException -> {
                        onEvent(MapEvent.DeniedLocationPermission)
                    }
                }
            }
        }
    }
    val scaffoldState = rememberScaffoldState()
    val snackbarInteractionSource = remember { MutableInteractionSource() }
    ObserveEffectsOnLifecycle(
        flow = effect
    ) {
        scope.launch {
            when (it) {
                is MapSideEffect.NavigateUp -> navController.navigateUp()
                is MapSideEffect.OpenPermissionSettingPage -> permissionsController.openAppSettings()
                is MapSideEffect.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    scaffoldState.snackbarHostState.showSnackbar(it.message)
                }
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            snackbarHost = { scaffoldState.snackbarHostState }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val selectedWeather by rememberUpdatedState(state.selectedWeather)
                val isPlaceWeatherVisible by remember {
                    derivedStateOf {
                        selectedWeather != null
                    }
                }
                val markers = remember(state.markers, state.selectedMarker) {
                    listOfNotNull(state.selectedMarker) + state.markers
                }
                GoogleMaps(
                    modifier = Modifier.fillMaxSize(),
                    isControlsVisible = !isPlaceWeatherVisible,
                    onMarkerClick = { onEvent(MapEvent.OnMarkerClick(it)) },
                    onMapClick = { onEvent(MapEvent.OnMapClick(it)) },
                    onMyLocationClick = { onEvent(MapEvent.OnMyLocationClick(it)) },
                    markers = markers,
                    cameraPosition = state.cameraPosition,
                    contentPadding = WindowInsets.safeDrawing.asPaddingValues()
                )
                MapSearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    onEvent = onEvent
                )
                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    visible = isPlaceWeatherVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2}),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                ) {
                    MapPlaceWeather(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        place = state.selectedPlace,
                        weather = state.selectedWeather
                    )
                }
                SnackbarHost(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .clickable(
                            interactionSource = snackbarInteractionSource,
                            indication = null
                        ) {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                        },
                    hostState = scaffoldState.snackbarHostState,
                )
            }
        }
    }
}