package home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import weather.domain.model.HourlyWeather

@Composable
fun HourlyWeatherCard(
    hourlyWeathers: List<HourlyWeather>,
    modifier: Modifier = Modifier,
    baseGradientColor: Color = MaterialTheme.colors.secondary,
    tint: Color = MaterialTheme.colors.onSecondary,
) {
    val itemLazyState = rememberLazyListState()
    val graphScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollableState { delta ->
        scope.launch {
            itemLazyState.scrollBy(-delta)
            graphScrollState.scrollBy(-delta)
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
    ) {
        val itemWidth = remember { 50.dp }
        LazyRow(
            state = itemLazyState,
            userScrollEnabled = false
        ) {
            items(hourlyWeathers, key = { it.time.toString() }) {
                HourlyWeatherItem(
                    hourlyWeather = it,
                    modifier = Modifier.width(itemWidth),
                    tint = tint
                )
            }
        }
        // Scroll wrapper of graph
        Box(
            modifier = Modifier
                .horizontalScroll(
                    state = graphScrollState,
                    enabled = false,
                    flingBehavior = ScrollableDefaults.flingBehavior()
                )
        ) {
            HourlyWeatherGraph(
                hourlyWeathers = hourlyWeathers,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(itemWidth * hourlyWeathers.size)
                    .height(40.dp),
                itemWidth = itemWidth,
                tint = tint
            )
        }
    }
}