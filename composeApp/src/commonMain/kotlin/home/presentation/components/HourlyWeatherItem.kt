package home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.AmPmMarker
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_temperature_unit_sign
import org.jetbrains.compose.resources.painterResource
import weather.domain.model.HourlyWeather
import kotlin.math.roundToInt

@Composable
fun HourlyWeatherItem(
    hourlyWeather: HourlyWeather,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colors.onSecondary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val timeFormat = remember {
            LocalTime.Format {
                amPmHour(Padding.NONE)
                char(' ')
                amPmMarker(AmPmMarker.AM.name, AmPmMarker.PM.name)
            }
        }
        val hourlyTimeText = remember(hourlyWeather.time.time) {
            hourlyWeather.time.time.format(timeFormat)
        }
        Text(
            text = hourlyTimeText,
            color = tint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
        )
        WeatherIcon(
            weatherType = hourlyWeather.weatherType,
            modifier = Modifier
                .padding(4.dp)
                .size(24.dp),
            colorFilter = ColorFilter.tint(tint)
        )
        Row(
            modifier = Modifier.padding(start = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${hourlyWeather.temperature.roundToInt()}",
                modifier = Modifier.padding(end = 0.5.dp),
                color = tint,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
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
        }
    }
}