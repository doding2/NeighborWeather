package home.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import weather.domain.model.HourlyWeather
import weather.domain.model.WeatherType
import kotlin.math.min

@Composable
fun HourlyPrecipitationGraph(
    hourlyWeathers: List<HourlyWeather>,
    modifier: Modifier = Modifier,
    displayedMaxPrecipitation: Double = 5.0,
    itemWidth: Dp = Dp.Unspecified,
    height: Dp = Dp.Unspecified,
    strokeWidth: Dp = 1.5.dp,
    lineColor: Color = MaterialTheme.colorScheme.onSecondary,
    fillColor: Color = MaterialTheme.colorScheme.onSecondary
) {
    val updatedHourlyWeathers by rememberUpdatedState(hourlyWeathers)
    Canvas(
        modifier = modifier
            .size(
                width = itemWidth * updatedHourlyWeathers.size,
                height = height
            ),
        contentDescription = "Precipitation graph of hourly weather"
    ) {
        val precipitationPath = Path()

        val itemWidthPx = itemWidth.toPx()
        val horizontalPadding = itemWidthPx / 8f
        val strokeWidthPx = strokeWidth.toPx()
        val heightPerPrecipitation = size.height / displayedMaxPrecipitation

        updatedHourlyWeathers.forEachIndexed { index, hourlyWeather ->
            // skip drawing with zero precipitation
            val isRainy = hourlyWeather.run {
                (weatherType == WeatherType.Rainy || weatherType == WeatherType.Drizzle
                        || weatherType == WeatherType.RainShower || weatherType == WeatherType.Snowy
                        || weatherType == WeatherType.FreezingRain || weatherType == WeatherType.FreezingDrizzle
                        || weatherType == WeatherType.SnowShower || weatherType == WeatherType.Thunderstorm
                ) && precipitation > 0.0
            }
            if (!isRainy) {
                return@forEachIndexed
            }

            val displayedPrecipitation = min(hourlyWeather.precipitation, displayedMaxPrecipitation)
            val itemHeight = displayedPrecipitation * heightPerPrecipitation

            val leftX = (itemWidthPx * index) + horizontalPadding
            val rightX = itemWidthPx * (index + 1)  - horizontalPadding
            val bottomY = size.height
            val topY = (bottomY - itemHeight).toFloat()

            // draw top line of precipitation bar
            precipitationPath.moveTo(leftX, topY)
            val firstControlPoint = Offset(
                x = leftX,
                y = topY + (rightX - leftX) / 16f
            )
            val secondControlPoint = Offset(
                x = rightX,
                y = topY - (rightX - leftX) / 16f
            )
            precipitationPath.cubicTo(
                firstControlPoint.x, firstControlPoint.y,
                secondControlPoint.x, secondControlPoint.y,
                rightX, topY
            )
            drawPath(
                path = precipitationPath,
                color = lineColor,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // draw border of precipitation bar
            val halfStrokeWidthPx = strokeWidthPx / 2f
            precipitationPath.moveTo(rightX + halfStrokeWidthPx, topY)
            precipitationPath.lineTo(rightX + halfStrokeWidthPx, bottomY)
            precipitationPath.lineTo(leftX - halfStrokeWidthPx, bottomY)
            precipitationPath.lineTo(leftX - halfStrokeWidthPx, topY)

            // fill body of bars
            drawPath(
                path = precipitationPath,
                color = fillColor,
                style = Fill
            )

            precipitationPath.reset()
        }
    }
}