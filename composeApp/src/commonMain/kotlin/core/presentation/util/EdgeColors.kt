package core.presentation.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import core.presentation.ui.DefaultDarkNavColor
import core.presentation.ui.DefaultLightNavColor

// https://gist.github.com/oianmol/52f25be2a0e8f0d9bd2ca16b5d308625
@Composable
expect fun EdgeColors(
    darkTheme: Boolean = isSystemInDarkTheme(),
    statusBarColor: Color = Color.Transparent,
    navBarColor: Color = if (darkTheme) DefaultDarkNavColor else DefaultLightNavColor
)