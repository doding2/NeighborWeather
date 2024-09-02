package home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import core.presentation.util.EdgeColors
import core.presentation.util.ObserveEffectsOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import map.presentation.MapScreen

@Serializable
data object HomeScreen

@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    effect: Flow<HomeSideEffect>,
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        EdgeColors(
            darkTheme = false
        )
        val snackbarHostState = remember {
            SnackbarHostState()
        }
        val snackbarInteractionSource = remember { MutableInteractionSource() }
        val scope = rememberCoroutineScope()
        ObserveEffectsOnLifecycle(
            flow = effect,
            snackbarHostState
        ) {
            scope.launch {
                when (it) {
                    is HomeSideEffect.NavigateToMap -> navController.navigate(MapScreen)
                    is HomeSideEffect.ShowSnackbar -> {
                        if (it.isImmediate) {
                            snackbarHostState.currentSnackbarData?.dismiss()
                        }
                        snackbarHostState.showSnackbar(it.message)
                    }
                }
            }
        }
        Scaffold(
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
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
                    modifier = Modifier.padding(innerPadding),
                ) {
                    state.weather?.current?.let { current ->
                        item {
                            Text(
                                text = "[${current.time}]\n${current.temperature.toString().padEnd(4, '0')} °C",
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                    state.weather?.hourly?.let { hourly ->
                        items(hourly.withIndex().toList(), key = { it.index }) {
                            Text(
                                text = "[${it.value.time.time}] ${it.value.temperature.toString().padEnd(4, '0')} °C",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                IconButton(
                    onClick = {
                        onEvent(HomeEvent.NavigateToMap)
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = "Navigate to map button",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .align(Alignment.TopEnd)
                        .shadow(5.dp, CircleShape)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                        .size(40.dp),
                )
            }
        }
    }
}