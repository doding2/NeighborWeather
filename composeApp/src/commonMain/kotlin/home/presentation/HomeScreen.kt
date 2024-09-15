package home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
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
import home.presentation.components.CurrentWeatherCard
import home.presentation.components.HomeBackground
import home.presentation.util.weatherOnPrimary
import home.presentation.util.weatherPrimary
import home.presentation.util.weatherSecondary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import weather.domain.model.WeatherType

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

    val weather by rememberUpdatedState(state.weather)
    val isWeatherLoaded by remember {
        derivedStateOf {
            weather != null
        }
    }
    val transition: Transition<WeatherType?> = updateTransition(
        targetState = weather?.current?.weatherType
    )
    val primary by transition.animateColor(
        transitionSpec = {
            if (transition.currentState == null) tween(0)
            else tween(1000)
         },
        label = "primary"
    ) { weatherPrimary(it) }
    val onPrimary by transition.animateColor(
        transitionSpec = {
            if (transition.currentState == null) tween(0)
            else tween(1000)
        },
        label = "onPrimary"
    ) { weatherOnPrimary(it) }
    val secondary by transition.animateColor(
        transitionSpec = {
            if (transition.currentState == null) tween(0)
            else tween(1000)
        },
        label = "secondary"
    ) { weatherSecondary(it) }

    ObserveEffectsOnLifecycle(
        flow = effect,
        snackbarHostState
    ) {
        scope.launch {
            when (it) {
                is HomeSideEffect.NavigateToMap -> {
                    navController.navigate("map")
                }
                HomeSideEffect.OpenPermissionSettingPage -> permissionsController.openAppSettings()
                is HomeSideEffect.ShowSnackbar -> {
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
                LazyColumn(
                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                ) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            AnimatedVisibility(
                                visible = isWeatherLoaded,
                                modifier = Modifier.sizeIn(maxWidth = 360.dp),
                                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2}),
                                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                            ) {
                                Box {
                                    CurrentWeatherCard(
                                        place = state.myPlace,
                                        weather = weather,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                top = 53.dp,
                                                bottom = 35.dp
                                            )
                                            .padding(horizontal = 20.dp),
                                        backgroundColor = primary,
                                        tint = onPrimary
                                    )
                                    IconButton(
                                        onClick = {
                                            weather?.current?.weatherType?.let {
                                                onEvent(HomeEvent.NavigateToMap(it))
                                            }
                                          },
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .windowInsetsPadding(WindowInsets.statusBars)
                                            .align(Alignment.TopEnd)
                                            .background(
                                                color = primary,
                                                shape = CircleShape
                                            )
                                            .size(48.dp),
                                        content = {
                                            Icon(
                                                imageVector = Icons.Rounded.Map,
                                                contentDescription = "Navigate to map button",
                                                modifier = Modifier.size(24.dp),
                                                tint = onPrimary
                                            )
                                        },
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }





            }
        }
    }
}