import androidx.compose.ui.window.ComposeUIViewController
import core.di.initKoin
import core.presentation.App

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}