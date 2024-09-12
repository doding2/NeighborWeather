package map.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.presentation.ui.theme.sunnyOnPrimary
import core.presentation.ui.theme.sunnyPrimary
import dev.jordond.compass.Place
import home.presentation.components.WeatherImage
import map.domain.model.toLocationName
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_temperature_unit_sign
import org.jetbrains.compose.resources.painterResource
import weather.domain.model.Weather
import kotlin.math.round

@Composable
fun MapPlaceWeather(
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

    weatherCache?.let {
        Column(
            modifier = modifier
                .background(
                    color = sunnyPrimary,
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(top = 20.dp, bottom = 32.dp, start = 30.dp, end = 30.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                WeatherImage(
                    weatherType = it.current.weatherType,
                    modifier = Modifier.size(36.dp),
                    colorFilter = ColorFilter.tint(sunnyOnPrimary)
                )
                Row(
                    modifier = Modifier.offset(y = (-4).dp),
                ) {
                    Text(
                        text = "${round(it.current.temperature).toInt()}",
                        modifier = Modifier.padding(start = 10.dp, end = 1.dp),
                        color = sunnyOnPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Image(
                        painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                        contentDescription = "Temperature unit sign",
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = 8.dp)
                            .size(7.5.dp),
                        alignment = Alignment.TopCenter,
                        colorFilter = ColorFilter.tint(sunnyOnPrimary)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = placeCache?.toLocationName() ?: "Unknown place",
                        color = sunnyOnPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = placeCache?.street ?: "",
                        color = sunnyOnPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.End,
                        lineHeight = 14.sp,
                    )
                }
            }
        }
    }
}