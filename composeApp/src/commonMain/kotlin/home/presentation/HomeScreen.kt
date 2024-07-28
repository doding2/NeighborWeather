package home.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            state.weather?.hourly?.temperature?.let { item ->
                items(item) {
                    Text(text = it.toString())
                }
            }
        }
    }
}