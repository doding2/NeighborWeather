package map.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.jordond.compass.Place
import map.domain.model.toLocationName
import weather.domain.model.Weather

@Composable
fun MapPlaceWeather(
    modifier: Modifier = Modifier,
    place: Place?,
    weather: Weather?
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
        Text(
            modifier = Modifier.padding(vertical = 4.dp),
            text = "${placeCache?.toLocationName()}"
        )
        Text(
            modifier = Modifier.padding(vertical = 4.dp),
            text = "${placeCache?.street}"
        )
        Text(
            modifier = Modifier.padding(vertical = 4.dp),
            text = buildString {
                if (weatherCache != null) {
                    append(weatherCache?.current?.temperature)
                    append('°')
                } else {
                    append(' ')
                }
            }
        )
    }
}