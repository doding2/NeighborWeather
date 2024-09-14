package core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.zeroValue
import platform.CoreGraphics.CGRect
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UINavigationBar
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.statusBarManager

@Composable
actual fun EdgeColors(
    darkTheme: Boolean,
    statusBarColor: Color,
    navBarColor: Color
) {
    val statusBar = rememberStatusBarView(darkTheme)
    SideEffect {
        statusBar.backgroundColor = statusBarColor.toUIColor()
        UINavigationBar.appearance().backgroundColor = navBarColor.toUIColor()
//        statusBar.tintColor = navBarColor.toUIColor()
//        UIApplication.sharedApplication.statusBarStyle
        val window = (UIApplication.sharedApplication.connectedScenes
            .firstOrNull() as? UIWindowScene)?.windows?.firstOrNull() as? UIWindow
        window?.overrideUserInterfaceStyle =
            if (darkTheme) UIUserInterfaceStyle.UIUserInterfaceStyleDark else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun rememberStatusBarView(darkTheme: Boolean): UIView = remember {
    val keyWindow: UIWindow? = UIApplication.sharedApplication.windows
        .firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
    val tag = 3848245L // https://stackoverflow.com/questions/56651245/how-to-change-the-status-bar-background-color-and-text-color-on-ios-13

//    keyWindow?.overrideUserInterfaceStyle =
//        if (darkTheme) UIUserInterfaceStyle.UIUserInterfaceStyleDark else UIUserInterfaceStyle.UIUserInterfaceStyleLight

    val windowStatusBarView = keyWindow?.viewWithTag(tag)
    if (windowStatusBarView != null) {
        windowStatusBarView
    } else {
        val height = keyWindow?.windowScene?.statusBarManager?.statusBarFrame
            ?: zeroValue<CGRect>()
        val statusBarView = UIView(frame = height)
        statusBarView.tag = tag
        statusBarView.layer.zPosition = 999999.0
        keyWindow?.addSubview(statusBarView)
        statusBarView
    }
}

private fun Color.toUIColor(): UIColor = UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble()
)