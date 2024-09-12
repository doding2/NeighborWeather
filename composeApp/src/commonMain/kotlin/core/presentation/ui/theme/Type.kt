package core.presentation.ui.theme

import androidx.compose.material.Typography
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
        h1 = h1.copy(fontFamily = fontFamily),
        h2 = h2.copy(fontFamily = fontFamily),
        h3 = h3.copy(fontFamily = fontFamily),
        h4 = h4.copy(fontFamily = fontFamily),
        h5 = h5.copy(fontFamily = fontFamily),
        h6 = h6.copy(fontFamily = fontFamily),
        subtitle1 = subtitle1.copy(fontFamily = fontFamily),
        subtitle2 = subtitle2.copy(fontFamily = fontFamily),
        body1 = body1.copy(fontFamily = fontFamily),
        body2 = body2.copy(fontFamily = fontFamily),
        button = button.copy(fontFamily = fontFamily),
        caption = caption.copy(fontFamily = fontFamily),
        overline = overline.copy(fontFamily = fontFamily)
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