package home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_cloudy
import neighborweather.composeapp.generated.resources.icon_cloudy_sunny
import neighborweather.composeapp.generated.resources.icon_cloudy_sunny_night
import neighborweather.composeapp.generated.resources.icon_drizzle
import neighborweather.composeapp.generated.resources.icon_drizzle_night
import neighborweather.composeapp.generated.resources.icon_fog
import neighborweather.composeapp.generated.resources.icon_freezing_drizzle
import neighborweather.composeapp.generated.resources.icon_freezing_drizzle_night
import neighborweather.composeapp.generated.resources.icon_freezing_rain
import neighborweather.composeapp.generated.resources.icon_freezing_rain_night
import neighborweather.composeapp.generated.resources.icon_rain_shower
import neighborweather.composeapp.generated.resources.icon_rain_shower_night
import neighborweather.composeapp.generated.resources.icon_rainy
import neighborweather.composeapp.generated.resources.icon_snow_shower
import neighborweather.composeapp.generated.resources.icon_snowy
import neighborweather.composeapp.generated.resources.icon_sunny
import neighborweather.composeapp.generated.resources.icon_sunny_night
import neighborweather.composeapp.generated.resources.icon_thunderstorm
import org.jetbrains.compose.resources.painterResource
import weather.domain.model.WeatherType

@Composable
fun WeatherImage(
    weatherType: WeatherType,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
    isNightTheme: Boolean = isSystemInDarkTheme()
) {
    val drawable = remember(weatherType) {
        when (weatherType) {
            WeatherType.Sunny -> {
                if (!isNightTheme) Res.drawable.icon_sunny
                else Res.drawable.icon_sunny_night
            }
            WeatherType.CloudySunny -> {
                if (!isNightTheme) Res.drawable.icon_cloudy_sunny
                else Res.drawable.icon_cloudy_sunny_night
            }
            WeatherType.Fog -> Res.drawable.icon_fog
            WeatherType.Cloudy -> Res.drawable.icon_cloudy
            WeatherType.Drizzle -> {
                if (!isNightTheme) Res.drawable.icon_drizzle
                else Res.drawable.icon_drizzle_night
            }
            WeatherType.FreezingDrizzle -> {
                if (!isNightTheme) Res.drawable.icon_freezing_drizzle
                else Res.drawable.icon_freezing_drizzle_night
            }
            WeatherType.Rainy -> Res.drawable.icon_rainy
            WeatherType.RainShower -> {
                if (!isNightTheme) Res.drawable.icon_rain_shower
                else Res.drawable.icon_rain_shower_night
            }
            WeatherType.FreezingRain -> {
                if (!isNightTheme) Res.drawable.icon_freezing_rain
                else Res.drawable.icon_freezing_rain_night
            }
            WeatherType.Snowy -> Res.drawable.icon_snowy
            WeatherType.SnowShower -> Res.drawable.icon_snow_shower
            WeatherType.Thunderstorm -> Res.drawable.icon_thunderstorm
            WeatherType.Other -> Res.drawable.icon_sunny
        }
    }
    Image(
        modifier = modifier,
        painter = painterResource(drawable),
        contentDescription = "Weather icon of $weatherType",
        colorFilter = colorFilter
    )
}