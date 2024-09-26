package core.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = neighborWeatherTypography(),
        content = content
    )
}