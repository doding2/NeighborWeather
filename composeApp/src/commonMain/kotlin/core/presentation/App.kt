package core.presentation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import core.presentation.navigation.Routes
import core.presentation.ui.theme.NeighborWeatherTheme
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
    NeighborWeatherTheme {
        KoinContext {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = Routes.Home
            ) {
                composable<Routes.Home> {
                    val viewModel = koinViewModel<HomeViewModel>()
                    HomeScreen(
                        state = viewModel.state,
                        onEvent = viewModel::onEvent,
                        effect = viewModel.effect,
                        navController = navController
                    )
                }
                composable<Routes.Map> {
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