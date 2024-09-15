package home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import core.presentation.navigation.Routes
import core.presentation.util.EdgeColors
import core.presentation.util.ObserveEffectsOnLifecycle
import core.presentation.util.animateWeatherColors
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import home.presentation.components.HomeBackground
import home.presentation.components.WeatherCardList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    effect: Flow<HomeSideEffect>,
    navController: NavController
) {
    EdgeColors(
        darkTheme = true,
        navBarColor = Color.Transparent
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
                onEvent(HomeEvent.AcceptedLocationPermission)
            } catch (e: Throwable) {
                when (e) {
                    is DeniedAlwaysException,
                    is DeniedException,
                    is RequestCanceledException -> {
                        onEvent(HomeEvent.DeniedLocationPermission)
                    }
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarInteractionSource = remember { MutableInteractionSource() }
    ObserveEffectsOnLifecycle(
        flow = effect,
        snackbarHostState
    ) {
        scope.launch {
            when (it) {
                is HomeSideEffect.NavigateToMap -> {
                    navController.navigate(Routes.Map)
                }
                HomeSideEffect.OpenPermissionSettingPage -> permissionsController.openAppSettings()
                is HomeSideEffect.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(it.message)
                }
            }
        }
    }

    val weatherColors = animateWeatherColors(state.myWeather?.current?.weatherType)
    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .clickable(
                            interactionSource = snackbarInteractionSource,
                            indication = null
                        ) {
                            snackbarHostState.currentSnackbarData?.dismiss()
                        }
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                state.backgroundImage?.let {
                    HomeBackground(
                        backgroundImage = it,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                WeatherCardList(
                    weather = state.myWeather,
                    place = state.myPlace,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    colors = weatherColors,
                    contentPadding = WindowInsets.safeDrawing.asPaddingValues()
                )


            }
        }
    }
}