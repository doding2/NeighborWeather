package map.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jordond.compass.Place
import home.presentation.components.WeatherIcon
import map.domain.util.toPlaceAddress
import map.domain.util.toPlaceIdentifier
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_temperature_unit_sign
import org.jetbrains.compose.resources.painterResource
import weather.domain.model.Weather

@Composable
fun MapPlaceWeatherCard(
    place: Place?,
    weather: Weather?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    tint: Color = MaterialTheme.colorScheme.onPrimary,
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
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(top = 20.dp, bottom = 32.dp, start = 30.dp, end = 30.dp),
    ) {
        weatherCache?.let {
            Row(verticalAlignment = Alignment.Top) {
                WeatherIcon(
                    weatherType = it.current.weatherType,
                    modifier = Modifier.size(36.dp),
                    colorFilter = ColorFilter.tint(tint)
                )
                Row(
                    modifier = Modifier.offset(y = (-4).dp),
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${it.current.temperature}",
                        color = tint,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Image(
                        painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                        contentDescription = "Temperature unit sign",
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = 7.dp)
                            .size(7.5.dp),
                        alignment = Alignment.TopCenter,
                        colorFilter = ColorFilter.tint(tint)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = placeCache?.toPlaceIdentifier() ?: "Unknown place",
                        color = tint,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = placeCache?.toPlaceAddress() ?: "Unknown place",
                        color = tint,
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