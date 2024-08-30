package map.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import core.presentation.util.ObserveEffectsOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import map.presentation.components.GoogleMaps

@Composable
fun MapScreen(
    state: MapState,
    onEvent: (MapEvent) -> Unit,
    effect: Flow<MapSideEffect>,
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        val scope = rememberCoroutineScope()
        ObserveEffectsOnLifecycle(
            flow = effect
        ) {
            scope.launch {
                // TODO: Handle side effect
            }
        }
        GoogleMaps(
            modifier = Modifier.fillMaxSize()
        )
    }
}