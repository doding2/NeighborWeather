package core.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import core.presentation.util.EdgeColors

@Composable
fun NeighborWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    EdgeColors(darkTheme = darkTheme)

    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = NeighborWeatherTypography(),
        content = content
    )
}