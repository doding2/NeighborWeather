package home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import core.presentation.ui.theme.sunnyOnPrimary
import core.presentation.ui.theme.sunnyPrimary
import core.presentation.util.EdgeColors
import core.presentation.util.ObserveEffectsOnLifecycle
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import home.presentation.components.HomeCurrentWeatherCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.background_sunny
import org.jetbrains.compose.resources.painterResource

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
                is HomeSideEffect.NavigateToMap -> navController.navigate("map")
                is HomeSideEffect.OpenPermissionSettingPage -> permissionsController.openAppSettings()
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
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(Res.drawable.background_sunny),
                    contentDescription = "Home background image",
                    contentScale = ContentScale.Crop
                )
                val weather by rememberUpdatedState(state.weather)
                val isWeatherLoaded by remember {
                    derivedStateOf {
                        weather != null
                    }
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
                                modifier = Modifier.sizeIn(maxWidth = 360.dp),
                                visible = isWeatherLoaded,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2}),
                                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                            ) {
                                HomeCurrentWeatherCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = 53.dp,
                                            bottom = 35.dp
                                        )
                                        .padding(horizontal = 20.dp),
                                    place = state.myPlace,
                                    weather = weather
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
//                    state.weather?.hourly?.let { hourly ->
//                        items(hourly.withIndex().toList(), key = { it.value.time.toString() }) {
//                            Text(
//                                modifier = Modifier.fillMaxWidth(),
//                                text = "[${it.value.time.time}] ${it.value.temperature.toString().padEnd(4, '0')} °C",
//                                color = Color.White
//                            )
//                        }
//                    }
                }
                IconButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .align(Alignment.TopEnd)
                        .shadow(5.dp, CircleShape)
                        .background(
                            color = sunnyPrimary,
                            shape = CircleShape
                        )
                        .size(48.dp),
                    onClick = { onEvent(HomeEvent.NavigateToMap) },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = "Navigate to map button",
                            modifier = Modifier.size(24.dp),
                            tint = sunnyOnPrimary
                        )
                    },
                )
            }
        }
    }
}