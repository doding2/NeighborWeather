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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun HourlyWeatherGraph(
    hourlyWeathers: List<HourlyWeather>,
    modifier: Modifier = Modifier,
    itemWidth: Dp = Dp.Unspecified,
    smoothness: Float = 0.3f,
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
//        val precipitationPath = Path()

        // slope of the line
        var dx = 0f
        var dy = 0f
        var prevCenter = Offset(0f, 0f)
        val itemWidthPx = itemWidth.toPx()

        updatedHourlyWeathers.forEachIndexed { index, weather ->

            // calculate center point
            val x = itemWidthPx * index
            val centerX = x + itemWidthPx / 2
            val centerY = (((maxTemperature - weather.temperature) /
                    (maxTemperature - minTemperature)) * size.height).toFloat()
            val center = Offset(centerX, centerY)

            // add temperature line to path
            // https://github.com/PaoloConte/smooth-line-chart/blob/master/SmoothChartSample/src/org/paoloconte/smoothchart/SmoothLineChart.java
            val prevWeather = updatedHourlyWeathers.getOrNull(index - 1)
            val nextWeather = updatedHourlyWeathers.getOrNull(index + 1)
            if (prevWeather != null) {
                val p = center

                // first control point
                val p0 = prevCenter
                val d0 = sqrt((p.x - p0.x).pow(2) + (p.y - p0.y).pow(2))
                val x1 = min(p0.x + dx * d0, (p0.x + p.x) / 2)
                val y1 = p0.y + dy * d0

                // second control point
                val nextX = if (nextWeather == null) x else itemWidthPx * (index + 1)
                val nextCenterX = if (nextWeather == null) centerX else nextX + itemWidthPx / 2
                val nextCenterY = if (nextWeather == null) centerY else {
                    (((maxTemperature - nextWeather.temperature) / (maxTemperature - minTemperature)) * size.height).toFloat()
                }
                val p1 = Offset(nextCenterX, nextCenterY)
                val d1 = sqrt((p1.x - p0.x).pow(2) + (p1.y - p0.y).pow(2))
                dx = (p1.x - p0.x) / d1 * smoothness
                dy = (p1.y - p0.y) / d1 * smoothness
                val x2 = max(p.x - dx * d0, (p0.x + p.x) / 2)
                val y2 = p.y - dy * d0

                temperaturePath.cubicTo(x1, y1, x2, y2, p.x, p.y)
            }

            // move focus
            temperaturePath.moveTo(center.x, center.y)
            prevCenter = center

            // draw temperature point
            drawCircle(
                color = tint,
                radius = 8f,
                center = center
            )
        }

        // draw temperature lines
        drawPath(
            path = temperaturePath,
            color = tint.copy(alpha = 0.5f),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
        )
    }
}