
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import home.presentation.HomeScreen
import home.presentation.HomeViewModel
import map.presentation.MapScreen
import map.presentation.MapViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinContext {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = HomeScreen
            ) {
                composable<HomeScreen> {
                    val viewModel = koinViewModel<HomeViewModel>()
                    HomeScreen(
                        state = viewModel.state,
                        onEvent = viewModel::onEvent,
                        effect = viewModel.effect,
                        navController = navController
                    )
                }
                composable<MapScreen> {
                    val viewModel = koinViewModel<MapViewModel>()
                    MapScreen(
                        state = viewModel.state,
                        onEvent = viewModel::onEvent,
                        effect = viewModel.effect,
                        navController = navController
                    )
                }
            }
        }
    }
}