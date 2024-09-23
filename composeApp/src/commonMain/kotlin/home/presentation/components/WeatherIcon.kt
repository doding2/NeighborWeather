package home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_clear
import neighborweather.composeapp.generated.resources.icon_clear_force_light
import neighborweather.composeapp.generated.resources.icon_cloudy
import neighborweather.composeapp.generated.resources.icon_drizzle
import neighborweather.composeapp.generated.resources.icon_drizzle_force_light
import neighborweather.composeapp.generated.resources.icon_fog
import neighborweather.composeapp.generated.resources.icon_freezing_drizzle
import neighborweather.composeapp.generated.resources.icon_freezing_drizzle_force_light
import neighborweather.composeapp.generated.resources.icon_freezing_rain
import neighborweather.composeapp.generated.resources.icon_mainly_clear
import neighborweather.composeapp.generated.resources.icon_mainly_clear_force_light
import neighborweather.composeapp.generated.resources.icon_rain_shower
import neighborweather.composeapp.generated.resources.icon_rain_shower_force_light
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
    forceLight: Boolean = false
) {
    val drawable = remember(weatherType) {
        when (weatherType) {
            WeatherType.Clear -> {
                if (forceLight) Res.drawable.icon_clear_force_light
                else Res.drawable.icon_clear
            }
            WeatherType.MainlyClear -> {
                if (forceLight) Res.drawable.icon_mainly_clear_force_light
                else Res.drawable.icon_mainly_clear
            }
            WeatherType.Fog -> Res.drawable.icon_fog
            WeatherType.Cloudy -> Res.drawable.icon_cloudy
            WeatherType.Drizzle -> {
                if (forceLight) Res.drawable.icon_drizzle_force_light
                else Res.drawable.icon_drizzle
            }
            WeatherType.FreezingDrizzle -> {
                if (forceLight) Res.drawable.icon_freezing_drizzle_force_light
                else Res.drawable.icon_freezing_drizzle
            }
            WeatherType.Rainy -> Res.drawable.icon_rainy
            WeatherType.RainShower -> {
                if (forceLight) Res.drawable.icon_rain_shower_force_light
                else Res.drawable.icon_rain_shower
            }
            WeatherType.FreezingRain -> {
                if (forceLight) Res.drawable.icon_freezing_rain
                else Res.drawable.icon_freezing_rain
            }
            WeatherType.Snowy -> Res.drawable.icon_snowy
            WeatherType.SnowShower -> Res.drawable.icon_snow_shower
            WeatherType.Thunderstorm -> Res.drawable.icon_thunderstorm
            WeatherType.Other -> {
                if (forceLight) Res.drawable.icon_clear_force_light
                else Res.drawable.icon_clear
            }
        }
    }
    Image(
        painter = painterResource(drawable),
        contentDescription = "Weather icon of $weatherType",
        modifier = modifier,
        colorFilter = colorFilter
    )
}