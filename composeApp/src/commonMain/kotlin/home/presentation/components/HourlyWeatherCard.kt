package home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import weather.domain.model.HourlyWeather

@Composable
fun HourlyWeatherCard(
    hourlyWeathers: List<HourlyWeather>,
    modifier: Modifier = Modifier,
    baseGradientColor: Color = MaterialTheme.colors.secondary,
    tint: Color = MaterialTheme.colors.onSecondary
) {
    Column(
        modifier
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        baseGradientColor.copy(alpha = 0.7f),
                        baseGradientColor.copy(alpha = 0.3f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                ),
                shape = RoundedCornerShape(25.dp)
            )
            .padding(top = 15.dp, bottom = 20.dp, start = 15.dp, end = 15.dp)
    ) {
        val updatedHourlyWeathers by rememberUpdatedState(hourlyWeathers)
        val topWeathers by remember {
            derivedStateOf { updatedHourlyWeathers.take(5) }
        }
        val bottomWeathers by remember {
            derivedStateOf { updatedHourlyWeathers.subList(6, 11) }
        }
        Row {
            topWeathers.forEach { hourlyWeather ->
                HourlyWeatherItem(
                    hourlyWeather = hourlyWeather,
                    modifier = Modifier.weight(1f),
                    tint = tint
                )
            }
        }
        Divider(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .padding(vertical = 10.dp),
            color = tint
        )
        Row {
            bottomWeathers.forEach { hourlyWeather ->
                HourlyWeatherItem(
                    hourlyWeather = hourlyWeather,
                    modifier = Modifier.weight(1f),
                    tint = tint
                )
            }
        }
    }
}