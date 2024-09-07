package map.presentation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import map.presentation.components.MapPlaceInfo
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
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarInteractionSource = remember { MutableInteractionSource() }
    ObserveEffectsOnLifecycle(
        flow = effect
    ) {
        scope.launch {
            when (it) {
                is MapSideEffect.NavigateUp -> navController.navigateUp()
                is MapSideEffect.OpenPermissionSettingPage -> permissionsController.openAppSettings()
                is MapSideEffect.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(it.message)
                }
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.clickable(
                        interactionSource = snackbarInteractionSource,
                        indication = null
                    ) {
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isPlaceInfoVisible = remember(state.selectedPlace) {
                    state.selectedPlace != null
                }
                val markers = remember(state.markers, state.selectedMarker) {
                    listOfNotNull(state.selectedMarker) + state.markers
                }
                GoogleMaps(
                    isControlsVisible = !isPlaceInfoVisible,
                    onMapClick = { onEvent(MapEvent.OnMapClick(it)) },
                    onMarkerClick = { onEvent(MapEvent.OnMarkerClick(it)) },
                    onMyLocationClick = { onEvent(MapEvent.OnMyLocationClick(it)) },
                    markers = markers,
                    cameraPosition = state.cameraPosition,
                    contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
                    modifier = Modifier.fillMaxSize()
                )
                MapSearchBar(
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                )
                AnimatedVisibility(
                    visible = isPlaceInfoVisible,
                    enter = slideInVertically(initialOffsetY = { it / 2}),
                    exit = slideOutVertically(targetOffsetY = { it / 2}),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    state.selectedPlace?.let { place ->
                        MapPlaceInfo(
                            place = place,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}