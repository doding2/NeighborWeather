package home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_precipitation_probability_high
import neighborweather.composeapp.generated.resources.icon_precipitation_probability_low
import neighborweather.composeapp.generated.resources.icon_precipitation_probability_mid
import neighborweather.composeapp.generated.resources.precipitation_probability_unit
import neighborweather.composeapp.generated.resources.precipitation_unit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import weather.domain.model.HourlyWeather
import weather.domain.model.WeatherType

@Composable
fun HourlyPrecipitationItem(
    hourlyWeather: HourlyWeather,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSecondary
) {
    val updatedHourlyWeather by rememberUpdatedState(hourlyWeather)
    val isRainy by remember {
        derivedStateOf {
            updatedHourlyWeather.run {
                (weatherType == WeatherType.Rainy || weatherType == WeatherType.Drizzle
                        || weatherType == WeatherType.RainShower || weatherType == WeatherType.Snowy
                        || weatherType == WeatherType.FreezingRain || weatherType == WeatherType.FreezingDrizzle
                        || weatherType == WeatherType.SnowShower || weatherType == WeatherType.Thunderstorm
                        ) && precipitation > 0.0
            }
        }
    }
    val precipitationProbabilityIcon by remember {
        derivedStateOf {
            updatedHourlyWeather.precipitationProbability.let {
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(precipitationProbabilityIcon),
                contentDescription = "Precipitation probability icon",
                modifier = Modifier
                    .padding(top = 1.5.dp)
                    .size(10.dp),
                tint = tint,
            )
            Text(
                text = "${updatedHourlyWeather.precipitationProbability.toInt()}",
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
        if (isRainy) {
            Row {
                Text(
                    text = "${updatedHourlyWeather.precipitation}",
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
                    text = stringResource(Res.string.precipitation_unit),
                    modifier =  Modifier.alignByBaseline(),
                    color = tint,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 8.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}