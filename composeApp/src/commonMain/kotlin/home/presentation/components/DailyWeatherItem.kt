package home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.toLocalDateTime
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_precipitation_probability_high
import neighborweather.composeapp.generated.resources.icon_precipitation_probability_low
import neighborweather.composeapp.generated.resources.icon_precipitation_probability_mid
import neighborweather.composeapp.generated.resources.icon_temperature_unit_sign
import neighborweather.composeapp.generated.resources.precipitation_probability_unit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import weather.domain.model.DailyWeather

@Composable
fun DailyWeatherItem(
    dailyWeather: DailyWeather,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colors.onSecondary,
) {
    val updatedDailyWeather by rememberUpdatedState(dailyWeather)
    val precipitationProbabilityIcon by remember {
        derivedStateOf {
            updatedDailyWeather.precipitationProbability.let {
                if (it >= 70) {
                    Res.drawable.icon_precipitation_probability_high
                } else if (it >= 30) {
                    Res.drawable.icon_precipitation_probability_mid
                } else {
                    Res.drawable.icon_precipitation_probability_low
                }
            }
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dayOfWeekFormat = remember {
            LocalDate.Format {
                dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            }
        }
        val dayOfWeekText by remember {
            derivedStateOf {
                val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val isToday = updatedDailyWeather.time == nowDate
                if (isToday) {
                    "Today"
                } else {
                    dayOfWeekFormat.format(updatedDailyWeather.time)
                }
            }
        }
        Text(
            text = dayOfWeekText,
            modifier = Modifier.weight(3f),
            color = tint,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(precipitationProbabilityIcon),
                contentDescription = "Precipitation probability icon",
                modifier = Modifier.size(10.dp),
                tint = tint,
            )
            Text(
                text = "${updatedDailyWeather.precipitationProbability.toInt()}",
                modifier = Modifier.alignByBaseline(),
                color = tint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = stringResource(Res.string.precipitation_probability_unit),
                modifier = Modifier.alignByBaseline(),
                color = tint,
                fontSize = 8.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 8.sp,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Box(modifier = Modifier.weight(0.9f)) {
            WeatherIcon(
                weatherType = updatedDailyWeather.weatherType,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(tint),
                darkTheme = false
            )
        }
        Row(
            modifier = Modifier.weight(0.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${updatedDailyWeather.temperatureMax.toInt()}",
                color = tint,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(0.5.dp))
            Image(
                painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                contentDescription = "Temperature unit sign",
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(top = 4.5.dp)
                    .size(5.dp),
                alignment = Alignment.TopCenter,
                colorFilter = ColorFilter.tint(tint)
            )
        }
        Spacer(modifier = Modifier.weight(0.2f))
        Row(
            modifier = Modifier.weight(0.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${updatedDailyWeather.temperatureMin.toInt()}",
                color = tint,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(0.5.dp))
            Image(
                painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                contentDescription = "Temperature unit sign",
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(top = 4.5.dp)
                    .size(5.dp),
                alignment = Alignment.TopCenter,
                colorFilter = ColorFilter.tint(tint)
            )
        }
    }
}