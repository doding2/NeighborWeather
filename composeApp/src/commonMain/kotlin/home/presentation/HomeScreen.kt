package home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import core.presentation.navigation.Routes
import core.presentation.util.EdgeColors
import core.presentation.util.ObserveEffectsOnLifecycle
import core.presentation.util.animateWeatherColors
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
    val weatherColors = animateWeatherColors(state.myWeather?.current?.weatherType)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarInteractionSource = remember { MutableInteractionSource() }
    val permissionsControllerFactory = rememberPermissionsControllerFactory()
    val permissionsController = remember(permissionsControllerFactory) {
        permissionsControllerFactory.createPermissionsController()
    }
    BindEffect(permissionsController)
    ObserveEffectsOnLifecycle(
        flow = effect,
        snackbarHostState
    ) {
        scope.launch {
            when (it) {
                is HomeSideEffect.NavigateToMap -> { navController.navigate(Routes.Map) }
                HomeSideEffect.OpenPermissionSettingPage -> { permissionsController.openAppSettings() }
                is HomeSideEffect.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = it.event.message,
                        actionLabel = it.event.action?.name,
                        duration = it.event.duration
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> { it.event.action?.action?.invoke() }
                        SnackbarResult.Dismissed -> {}
                    }
                }
            }
        }
    }
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
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            state.backgroundImage?.let {
                HomeBackground(
                    backgroundImage = it,
                    modifier = Modifier.fillMaxSize()
                )
            }
//            HomeNavigationDrawer(
//                width = getScreenWidth() * 0.6f,
//                height = getScreenHeight(),
//                modifier = Modifier,
//                colors = weatherColors,
//            ) {
//                WeatherCardList(
//                    weather = state.myWeather,
//                    place = state.myPlace,
//                    onEvent = onEvent,
//                    modifier = Modifier.fillMaxSize(),
//                    colors = weatherColors,
//                    contentPadding = WindowInsets.safeDrawing.asPaddingValues()
//                )
//            }
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