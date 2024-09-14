package home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.presentation.ui.theme.sunnyDayOnPrimary
import core.presentation.ui.theme.sunnyDayPrimary
import dev.jordond.compass.Place
import map.util.toPlaceIdentifier
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.home_current_weather_title
import neighborweather.composeapp.generated.resources.icon_temperature_unit_sign
import neighborweather.composeapp.generated.resources.precipitation_probability_unit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import weather.domain.model.Weather
import kotlin.math.round

@Composable
fun CurrentWeatherCard(
    place: Place?,
    weather: Weather?,
    modifier: Modifier = Modifier,
) {
    /*
    When state value become null,
    Text in MapPlace will be shown "null" text.
    To avoid this "null" text, cache state values.
     */
    var placeCache by remember { mutableStateOf<Place?>(null) }
    var weatherCache by remember { mutableStateOf<Weather?>(null) }
    LaunchedEffect(place) {
        if (place != null) { placeCache = place }
    }
    LaunchedEffect(weather) {
        if (weather != null) { weatherCache = weather }
    }
    Column(
        modifier = modifier
            .aspectRatio(360f / 380f)
            .background(
                color = sunnyDayPrimary,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(horizontal = 15.dp),
        verticalArrangement = Arrangement.Center
    ) {
        weatherCache?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // padding of each items: 25, 5, -10, 15, 15, 15, 28 -> 103 - 10
                // -> approximately round to to 100
                Spacer(modifier = Modifier.weight(0.25f))
                Text(
                    text = stringResource(Res.string.home_current_weather_title),
                    color = sunnyDayOnPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(0.05f))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    WeatherImage(
                        weatherType = it.current.weatherType,
                        modifier = Modifier.size(72.dp),
                        colorFilter = ColorFilter.tint(sunnyDayOnPrimary)
                    )
                    Text(
                        text = "${round(it.current.temperature).toInt()}",
                        modifier = Modifier.padding(start = 20.dp, end = 2.dp),
                        color = sunnyDayOnPrimary,
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Image(
                        painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                        contentDescription = "Temperature unit sign",
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = 23.dp),
                        alignment = Alignment.TopCenter,
                        colorFilter = ColorFilter.tint(sunnyDayOnPrimary)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    text = it.current.weatherType.toString(),
                    modifier = Modifier.offset(y = (-10).dp),
                    color = sunnyDayOnPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(0.15f))
                Text(
                    text = placeCache?.toPlaceIdentifier() ?: "Unknown place",
                    color = sunnyDayOnPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(0.15f))
                Text(
                    text = it.current.time.toString(),
                    color = sunnyDayOnPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(0.15f))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Feels like ${round(it.current.apparentTemperature).toInt()}",
                        color = sunnyDayOnPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Image(
                        painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                        contentDescription = "Temperature unit sign",
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = 4.dp)
                            .size(5.dp),
                        alignment = Alignment.TopCenter,
                        colorFilter = ColorFilter.tint(sunnyDayOnPrimary)
                    )
                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(1.dp)
                            .height(10.dp)
                            .background(
                                color = sunnyDayOnPrimary,
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                    Text(
                        text = "Rain by ${it.current.precipitationProbability}${stringResource(Res.string.precipitation_probability_unit)}",
                        color = sunnyDayOnPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.weight(0.28f))
            }
        }
    }
}