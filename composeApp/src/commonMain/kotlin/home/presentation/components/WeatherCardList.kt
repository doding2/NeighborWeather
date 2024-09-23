package home.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Colors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.jordond.compass.Place
import home.presentation.HomeEvent
import weather.domain.model.Weather

@Composable
fun WeatherCardList(
    weather: Weather?,
    place: Place?,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier,
    colors: Colors = MaterialTheme.colors,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val updatedWeather by rememberUpdatedState(weather)
    val isWeatherVisible by remember { derivedStateOf { updatedWeather != null } }
    var hourlyWeatherCardSize by remember { mutableStateOf(IntSize.Zero) }
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isWeatherVisible,
            modifier = Modifier
                .sizeIn(maxWidth = 360.dp)
                .zIndex(2f),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            Box {
                CurrentWeatherCard(
                    place = place,
                    currentWeather = updatedWeather?.current,
                    modifier = Modifier
                        .padding(top = 28.dp, start = 28.dp, end = 28.dp)
                        .fillMaxWidth(),
                    backgroundColor = colors.primary,
                    tint = colors.onPrimary,
                )
                IconButton(
                    onClick = { onEvent(HomeEvent.NavigateToMap) },
                    modifier = Modifier
                        .graphicsLayer {
                            val currentWidthPx = 304.dp.toPx()
                            val currentHeightPx = 324.dp.toPx()
                            val middlePaddingPx = 15.dp.toPx()
                            val currentBottomPx = currentHeightPx - middlePaddingPx
                            val hourlyTopPx = currentHeightPx + middlePaddingPx

                            val hourlyWidth = hourlyWeatherCardSize.width - 2f * 28.dp.toPx()
                            val widthDiff = (hourlyWidth - currentWidthPx) / 2f

                            translationX = if (scrollState.value < currentBottomPx) {
                                0f
                            } else if (scrollState.value >= currentBottomPx && scrollState.value < hourlyTopPx) {
                                widthDiff * (scrollState.value - currentBottomPx) / (middlePaddingPx * 2)
                            } else {
                                widthDiff
                            }
                            translationY = scrollState.value.toFloat()
                        }
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = colors.primary,
                            shape = CircleShape
                        )
                        .size(48.dp),
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = "Navigate to map button",
                            modifier = Modifier.size(24.dp),
                            tint = colors.onPrimary
                        )
                    },
                )
            }
        }
        AnimatedVisibility(
            visible = isWeatherVisible,
            modifier = Modifier
                .onGloballyPositioned { hourlyWeatherCardSize = it.size }
                .sizeIn(maxWidth = 500.dp)
                .zIndex(1f),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            HourlyWeatherCard(
                hourlyWeathers = updatedWeather?.hourly ?: emptyList(),
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 15.dp)
                    .fillMaxWidth(),
                baseGradientColor = colors.secondary,
                tint = colors.onSecondary
            )
        }
        AnimatedVisibility(
            visible = isWeatherVisible,
            modifier = Modifier
                .sizeIn(maxWidth = 500.dp)
                .zIndex(0f),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            DailyWeatherCard(
                dailyWeathers = updatedWeather?.daily ?: emptyList(),
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 28.dp)
                    .fillMaxWidth(),
                baseGradientColor = colors.secondary,
                tint = colors.onSecondary
            )
        }
    }
}