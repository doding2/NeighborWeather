import androidx.compose.ui.window.ComposeUIViewController
import core.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}