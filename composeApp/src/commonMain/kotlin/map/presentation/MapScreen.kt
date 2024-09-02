package map.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import kotlinx.serialization.Serializable
import map.presentation.components.GoogleMaps

@Serializable
data object MapScreen

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
        ObserveEffectsOnLifecycle(
            flow = effect
        ) {
            scope.launch {
                when (it) {
                    MapSideEffect.NavigateUp -> navController.navigateUp()
                    MapSideEffect.OpenPermissionSettingPage -> permissionsController.openAppSettings()
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            GoogleMaps(
                modifier = Modifier.fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                IconButton(
                    onClick = {
                        onEvent(MapEvent.NavigateUp)
                    },
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back button",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier
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