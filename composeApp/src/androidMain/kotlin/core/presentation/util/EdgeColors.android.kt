package core.presentation.util

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun EdgeColors(
    darkTheme: Boolean,
    statusBarColor: Color,
    navBarColor: Color
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }

            if (Build.VERSION.SDK_INT >= 29) {
                val isContrastEnforced = navBarColor != Color.Transparent
                window.isNavigationBarContrastEnforced = isContrastEnforced
            }
        }
    }
}