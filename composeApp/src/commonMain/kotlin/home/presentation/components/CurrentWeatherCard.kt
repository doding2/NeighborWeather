package home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jordond.compass.Place
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.AmPmMarker
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import map.domain.util.toPlaceIdentifier
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.home_current_weather_title
import neighborweather.composeapp.generated.resources.humidity_unit
import neighborweather.composeapp.generated.resources.icon_temperature_unit_sign
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import weather.domain.model.CurrentWeather
import weather.domain.model.toFormattedName

@Composable
fun CurrentWeatherCard(
    place: Place?,
    currentWeather: CurrentWeather?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primary,
    tint: Color = MaterialTheme.colors.onPrimary
) {
    /*
    When state value become null,
    Text in MapPlace will be shown "null" text.
    To avoid this "null" text, cache state values.
     */
    var placeCache by remember { mutableStateOf<Place?>(null) }
    var currentWeatherCache by remember { mutableStateOf<CurrentWeather?>(null) }
    LaunchedEffect(place) {
        if (place != null) { placeCache = place }
    }
    LaunchedEffect(currentWeather) {
        if (currentWeather != null) { currentWeatherCache = currentWeather }
    }
    Column(
        modifier = modifier
            .aspectRatio(360f / 380f)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(horizontal = 15.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        currentWeatherCache?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // padding of each items: 25, 5, -10, 15, 15, 15, 35 -> 110 - 10
                // -> approximately round to to 100
                Spacer(modifier = Modifier.weight(0.25f))
                Text(
                    text = stringResource(Res.string.home_current_weather_title),
                    color = tint,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(0.05f))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherIcon(
                        weatherType = it.weatherType,
                        modifier = Modifier.size(60.dp),
                        colorFilter = ColorFilter.tint(tint)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "${it.temperature}",
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = tint,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Image(
                        painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                        contentDescription = "Temperature unit sign",
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = 24.dp)
                            .size(12.dp),
                        alignment = Alignment.TopCenter,
                        colorFilter = ColorFilter.tint(tint)
                    )
                }
                Text(
                    text = it.weatherType.toFormattedName(),
                    color = tint,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(0.15f))
                Text(
                    text = placeCache?.toPlaceIdentifier() ?: "Unknown place",
                    color = tint,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(0.15f))
                val datetimeFormatter = remember {
                    LocalDateTime.Format {
                        dayOfMonth()
                        char(' ')
                        monthName(MonthNames.ENGLISH_ABBREVIATED)
                        char(' ')
                        year()
                        char(' ')
                        amPmHour()
                        char(':')
                        minute()
                        char(' ')
                        amPmMarker(AmPmMarker.AM.name, AmPmMarker.PM.name)
                    }
                }
                Text(
                    text = it.time.format(datetimeFormatter),
                    color = tint,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(0.15f))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Feels like ${it.apparentTemperature}",
                        color = tint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(0.5.dp))
                    Image(
                        painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                        contentDescription = "Temperature unit sign",
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = 4.dp)
                            .size(5.dp),
                        alignment = Alignment.TopCenter,
                        colorFilter = ColorFilter.tint(tint)
                    )
                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(1.dp)
                            .height(10.dp)
                            .background(
                                color = tint,
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                    Text(
                        text = "Humidity ${it.relativeHumidity}${stringResource(Res.string.humidity_unit)}",
                        color = tint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.weight(0.35f))
            }
        }
    }
}