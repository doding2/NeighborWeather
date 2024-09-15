package core.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import core.presentation.util.EdgeColors

@Composable
fun NeighborWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    EdgeColors(
        darkTheme = darkTheme,
        navBarColor = Color.Transparent
    )

    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = neighborWeatherTypography(),
        content = content
    )
}