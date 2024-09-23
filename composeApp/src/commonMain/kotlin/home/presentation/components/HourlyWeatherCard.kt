package home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import weather.domain.model.HourlyWeather
import weather.domain.model.WeatherType

@Composable
fun HourlyWeatherCard(
    hourlyWeathers: List<HourlyWeather>,
    modifier: Modifier = Modifier,
    baseGradientColor: Color = MaterialTheme.colors.secondary,
    tint: Color = MaterialTheme.colors.onSecondary,
) {
    val updatedHourlyWeathers by rememberUpdatedState(hourlyWeathers)
    val precipitationItemsHeight by remember {
        derivedStateOf {
            val containsRainy = updatedHourlyWeathers.any {
                val isRainy = it.run {
                    (weatherType == WeatherType.Rainy || weatherType == WeatherType.Drizzle
                            || weatherType == WeatherType.RainShower || weatherType == WeatherType.Snowy
                            || weatherType == WeatherType.FreezingRain || weatherType == WeatherType.FreezingDrizzle
                            || weatherType == WeatherType.SnowShower || weatherType == WeatherType.Thunderstorm
                            ) && precipitation > 0.0
                }
                isRainy
            }
            if (containsRainy) 25.dp else 12.5.dp
        }
    }
    val weatherItemLazyState = rememberLazyListState()
    val precipitationItemLazyState = rememberLazyListState()
    val temperatureGraphScrollState = rememberScrollState()
    val precipitationGraphScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollableState { delta ->
        scope.launch {
            weatherItemLazyState.scrollBy(-delta)
            precipitationItemLazyState.scrollBy(-delta)
            temperatureGraphScrollState.scrollBy(-delta)
            precipitationGraphScrollState.scrollBy(-delta)
        }
        delta
    }
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
            .scrollable(
                state = scrollState,
                orientation = Orientation.Horizontal,
                flingBehavior = ScrollableDefaults.flingBehavior()
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val itemWidth = remember { 50.dp }
        LazyRow(
            state = weatherItemLazyState,
            userScrollEnabled = false
        ) {
            items(updatedHourlyWeathers, key = { it.time.toString() }) {
                HourlyWeatherItem(
                    hourlyWeather = it,
                    modifier = Modifier.width(itemWidth),
                    tint = tint
                )
            }
        }
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            HourlyTemperatureGraph(
                hourlyWeathers = updatedHourlyWeathers,
                modifier = Modifier
                    .horizontalScroll(
                        state = temperatureGraphScrollState,
                        enabled = false,
                        flingBehavior = ScrollableDefaults.flingBehavior()
                    ),
                itemWidth = itemWidth,
                height = 50.dp,
                tint = tint
            )
            HourlyPrecipitationGraph(
                hourlyWeathers = updatedHourlyWeathers,
                modifier = Modifier
                    .horizontalScroll(
                        state = precipitationGraphScrollState,
                        enabled = false,
                        flingBehavior = ScrollableDefaults.flingBehavior()
                    ),
                itemWidth = itemWidth,
                height = 50.dp,
                lineColor = tint,
                fillColor = tint.copy(alpha = 0.3f)
            )
        }
        LazyRow(
            state = precipitationItemLazyState,
            userScrollEnabled = false
        ) {
            items(updatedHourlyWeathers, key = { it.time.toString() }) {
                HourlyPrecipitationItem(
                    hourlyWeather = it,
                    modifier = Modifier.size(
                        width = itemWidth,
                        height = precipitationItemsHeight
                    ),
                    tint = tint
                )
            }
        }
    }
}