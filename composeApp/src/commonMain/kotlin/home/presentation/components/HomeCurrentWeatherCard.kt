package home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jordond.compass.Place
import map.domain.model.toLocationName
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.precipitation_probability_unit
import neighborweather.composeapp.generated.resources.temperature_unit
import org.jetbrains.compose.resources.stringResource
import weather.domain.model.Weather
import kotlin.math.round

@Composable
fun HomeCurrentWeatherCard(
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
            .background(
                color = Color.White,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(top = 20.dp, bottom = 32.dp, start = 30.dp, end = 30.dp)
    ) {
        weatherCache?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    WeatherImage(
                        modifier = Modifier
                            .size(72.dp),
                        weatherType = it.current.weatherType,
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Text(
                        modifier = Modifier.padding(start = 20.dp),
                        text = "${round(it.current.temperature).toInt()}",
                        color = Color.Black,
                        fontSize = 100.sp,
                    )
                    Text(
                        modifier = Modifier.align(Alignment.Top),
                        text = stringResource(Res.string.temperature_unit),
                        color = Color.Black,
                        fontSize = 60.sp,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    modifier = Modifier.padding(top = 30.dp),
                    text = it.current.weatherType.toString(),
                    color = Color.Black,
                    fontSize = 20.sp
                )
                Text(
                    modifier = Modifier.padding(top = 15.dp),
                    text = placeCache?.toLocationName() ?: "Unknown place",
                    color = Color.Black,
                    fontSize = 15.sp
                )
                Text(
                    modifier = Modifier.padding(top = 15.dp),
                    text = it.current.time.toString(),
                    color = Color.Black,
                    fontSize = 15.sp
                )
                Row(
                    modifier = Modifier.padding(top = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Feels like ${round(it.current.apparentTemperature).toInt()}",
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(1.dp)
                            .height(10.dp)
                            .background(
                                color = Color.Black,
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                    Text(
                        text = "Rain by ${it.current.precipitationProbability}${stringResource(Res.string.precipitation_probability_unit)}",
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}