package home.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import weather.domain.model.HourlyWeather

@Composable
fun HourlyWeatherGraph(
    hourlyWeathers: List<HourlyWeather>,
    modifier: Modifier = Modifier,
    itemWidth: Dp = Dp.Unspecified,
    temperatureColor: Color = MaterialTheme.colors.secondary,
    precipitationColor: Color = MaterialTheme.colors.secondary,
    tint: Color = MaterialTheme.colors.onSecondary
) {
    val updatedHourlyWeathers by rememberUpdatedState(hourlyWeathers)
    val maxTemperature by remember {
        derivedStateOf { updatedHourlyWeathers.maxOf { it.temperature } }
    }
    val minTemperature by remember {
        derivedStateOf { updatedHourlyWeathers.minOf { it.temperature } }
    }
    Canvas(
        modifier = modifier,
        contentDescription = "Temperature and precipitation graph of hourly weather"
    ) {
        val temperaturePath = Path()
        val precipitationPath = Path()

        updatedHourlyWeathers.forEachIndexed { index, hourlyWeather ->
            val x = itemWidth.toPx() * index

            // draw temperature point
            val centerX = x + itemWidth.toPx() / 2
            val centerY = (((maxTemperature - hourlyWeather.temperature) /
                    (maxTemperature - minTemperature)) * size.height).toFloat()
            drawCircle(
                color = tint,
                radius = 8f,
                center = Offset(centerX, centerY)
            )

            // add temperature line to path
            val previousHourlyWeather = updatedHourlyWeathers.getOrNull(index - 1)
            if (previousHourlyWeather != null) {
                temperaturePath.lineTo(centerX, centerY)
            }
            temperaturePath.moveTo(centerX, centerY)
        }

        // draw temperature line
        drawPath(
            path = temperaturePath,
            color = tint.copy(alpha = 0.3f),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
        )
    }
}