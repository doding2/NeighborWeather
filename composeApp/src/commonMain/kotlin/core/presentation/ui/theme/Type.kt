package core.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import neighborweather.composeapp.generated.resources.PoppinsBlackKr
import neighborweather.composeapp.generated.resources.PoppinsBoldKr
import neighborweather.composeapp.generated.resources.PoppinsExtraBoldKr
import neighborweather.composeapp.generated.resources.PoppinsExtraLightKr
import neighborweather.composeapp.generated.resources.PoppinsLightKr
import neighborweather.composeapp.generated.resources.PoppinsMediumKr
import neighborweather.composeapp.generated.resources.PoppinsRegularKr
import neighborweather.composeapp.generated.resources.PoppinsSemiBoldKr
import neighborweather.composeapp.generated.resources.PoppinsThinKr
import neighborweather.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun neighborWeatherTypography() = Typography().run {
    val fontFamily = poppinsKr()
    copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily)
    )
}

@Composable
fun poppinsKr() = FontFamily(
    Font(Res.font.PoppinsThinKr, FontWeight.Thin, FontStyle.Normal),
    Font(Res.font.PoppinsExtraLightKr, FontWeight.ExtraLight, FontStyle.Normal),
    Font(Res.font.PoppinsLightKr, FontWeight.Light, FontStyle.Normal),
    Font(Res.font.PoppinsRegularKr, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.PoppinsMediumKr, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.PoppinsSemiBoldKr, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.PoppinsBoldKr, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.PoppinsExtraBoldKr, FontWeight.ExtraBold, FontStyle.Normal),
    Font(Res.font.PoppinsBlackKr, FontWeight.Black, FontStyle.Normal),
)