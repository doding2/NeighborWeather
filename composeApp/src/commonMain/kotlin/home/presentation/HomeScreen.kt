package home.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import core.util.ObserveEffectsOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
        val snackbarHostState = remember {
            SnackbarHostState()
        }
        val scope = rememberCoroutineScope()
        ObserveEffectsOnLifecycle(
            flow = effect,
            snackbarHostState
        ) {
            scope.launch {
                when (it) {
                    is HomeSideEffect.ShowSnackbar -> {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(it.message)
                    }
                }
            }
        }
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding)
            ) {
                state.weather?.current?.let { current ->
                    item {
                        Text(
                            text = "${current.time.time}: ${current.temperature.toString().padEnd(5, '0')} °C",
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
                            text = "${it.value.time.time}: ${it.value.temperature.toString().padEnd(5, '0')} °C",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}