package map.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
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
                } catch (e: DeniedAlwaysException) {
                    onEvent(MapEvent.DeniedAlwaysLocationPermission)
                } catch (e: DeniedException) {
                    onEvent(MapEvent.DeniedLocationPermission)
                } catch (e: RequestCanceledException) {
                    onEvent(MapEvent.CanceledLocationPermission)
                }
            }
        }
        ObserveEffectsOnLifecycle(
            flow = effect
        ) {
            scope.launch {
                // TODO: Handle side effect
                when (it) {
                    MapSideEffect.OpenPermissionSettingPage -> permissionsController.openAppSettings()
                }
            }
        }
        GoogleMaps(
            modifier = Modifier.fillMaxSize()
        )
    }
}