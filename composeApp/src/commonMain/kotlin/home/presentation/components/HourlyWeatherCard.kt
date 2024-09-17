package home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    tint: Color = MaterialTheme.colors.onSecondary,
) {
    Column(
        modifier = modifier
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
            .horizontalScroll(rememberScrollState()),
    ) {
        val itemWidth = remember { 50.dp }
        Row {
            hourlyWeathers.forEach { hourlyWeather ->
                key(hourlyWeather.time.toString()) {
                    HourlyWeatherItem(
                        hourlyWeather = hourlyWeather,
                        modifier = Modifier.width(itemWidth),
                        tint = tint
                    )
                }
            }
        }
        HourlyWeatherGraph(
            hourlyWeathers = hourlyWeathers,
            modifier = Modifier
                .padding(top = 4.dp)
                .height(40.dp),
            itemWidth = itemWidth,
            temperatureColor = Color.Red,
            precipitationColor = Color.Cyan,
            tint = tint
        )
    }
}