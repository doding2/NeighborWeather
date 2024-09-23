package home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_clear
import neighborweather.composeapp.generated.resources.icon_clear_dark
import neighborweather.composeapp.generated.resources.icon_cloudy
import neighborweather.composeapp.generated.resources.icon_drizzle
import neighborweather.composeapp.generated.resources.icon_drizzle_dark
import neighborweather.composeapp.generated.resources.icon_fog
import neighborweather.composeapp.generated.resources.icon_freezing_drizzle
import neighborweather.composeapp.generated.resources.icon_freezing_drizzle_dark
import neighborweather.composeapp.generated.resources.icon_freezing_rain
import neighborweather.composeapp.generated.resources.icon_freezing_rain_dark
import neighborweather.composeapp.generated.resources.icon_mainly_clear
import neighborweather.composeapp.generated.resources.icon_mainly_clear_dark
import neighborweather.composeapp.generated.resources.icon_rain_shower
import neighborweather.composeapp.generated.resources.icon_rain_shower_dark
import neighborweather.composeapp.generated.resources.icon_rainy
import neighborweather.composeapp.generated.resources.icon_snow_shower
import neighborweather.composeapp.generated.resources.icon_snowy
import neighborweather.composeapp.generated.resources.icon_thunderstorm
import org.jetbrains.compose.resources.painterResource
import weather.domain.model.WeatherType

@Composable
fun WeatherIcon(
    weatherType: WeatherType,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val updatedWeatherType by rememberUpdatedState(weatherType)
    val updatedDarkTheme by rememberUpdatedState(darkTheme)
    val iconDrawable by remember {
        derivedStateOf {
            when (updatedWeatherType) {
                WeatherType.Clear -> {
                    if (updatedDarkTheme) Res.drawable.icon_clear_dark
                    else Res.drawable.icon_clear
                }
                WeatherType.MainlyClear -> {
                    if (updatedDarkTheme) Res.drawable.icon_mainly_clear_dark
                    else Res.drawable.icon_mainly_clear
                }
                WeatherType.Fog -> Res.drawable.icon_fog
                WeatherType.Cloudy -> Res.drawable.icon_cloudy
                WeatherType.Drizzle -> {
                    if (updatedDarkTheme) Res.drawable.icon_drizzle_dark
                    else Res.drawable.icon_drizzle
                }
                WeatherType.FreezingDrizzle -> {
                    if (updatedDarkTheme) Res.drawable.icon_freezing_drizzle_dark
                    else Res.drawable.icon_freezing_drizzle
                }
                WeatherType.Rainy -> Res.drawable.icon_rainy
                WeatherType.RainShower -> {
                    if (updatedDarkTheme) Res.drawable.icon_rain_shower_dark
                    else Res.drawable.icon_rain_shower
                }
                WeatherType.FreezingRain -> {
                    if (updatedDarkTheme) Res.drawable.icon_freezing_rain_dark
                    else Res.drawable.icon_freezing_rain
                }
                WeatherType.Snowy -> Res.drawable.icon_snowy
                WeatherType.SnowShower -> Res.drawable.icon_snow_shower
                WeatherType.Thunderstorm -> Res.drawable.icon_thunderstorm
                WeatherType.Other -> {
                    if (updatedDarkTheme) Res.drawable.icon_clear_dark
                    else Res.drawable.icon_clear
                }
            }
        }
    }
    Image(
        painter = painterResource(iconDrawable),
        contentDescription = "Weather icon of $updatedWeatherType",
        modifier = modifier,
        colorFilter = colorFilter
    )
}