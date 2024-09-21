package home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.precipitation_probability_unit
import neighborweather.composeapp.generated.resources.precipitation_unit
import org.jetbrains.compose.resources.stringResource
import weather.domain.model.HourlyWeather
import weather.domain.model.WeatherType

@Composable
fun HourlyPrecipitationItem(
    hourlyWeather: HourlyWeather,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colors.onSecondary
) {
    val updatedHourlyWeather by rememberUpdatedState(hourlyWeather)
    val isRainy by remember {
        derivedStateOf {
            val isRainy = updatedHourlyWeather.run {
                (weatherType == WeatherType.Rainy || weatherType == WeatherType.Drizzle
                        || weatherType == WeatherType.RainShower || weatherType == WeatherType.Snowy
                        || weatherType == WeatherType.FreezingRain || weatherType == WeatherType.FreezingDrizzle
                        || weatherType == WeatherType.SnowShower || weatherType == WeatherType.Thunderstorm
                        ) && precipitation > 0.0
            }
            isRainy
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "${updatedHourlyWeather.precipitationProbability}",
                modifier = Modifier.alignByBaseline(),
                color = tint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.precipitation_probability_unit),
                modifier = Modifier.alignByBaseline(),
                color = tint,
                fontSize = 8.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 8.sp,
                textAlign = TextAlign.Center
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
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(Res.string.precipitation_unit),
                    modifier =  Modifier.alignByBaseline(),
                    color = tint,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 8.sp,
                )
            }
        }
    }
}