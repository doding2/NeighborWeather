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
    val isCurrentWeatherVisible by remember { derivedStateOf { updatedWeather?.current != null } }
    val isHourlyWeatherVisible by remember { derivedStateOf { !updatedWeather?.hourly.isNullOrEmpty() } }
    val isDailyWeatherVisible by remember { derivedStateOf { !updatedWeather?.daily.isNullOrEmpty() } }
    val scrollState = rememberScrollState()
    var currentWeatherSize by remember { mutableStateOf(IntSize.Zero) }
    var hourlyWeatherSize by remember { mutableStateOf(IntSize.Zero) }
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isCurrentWeatherVisible,
            modifier = Modifier
                .sizeIn(maxWidth = 360.dp)
                .zIndex(2f),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            Box(contentAlignment = Alignment.Center) {
                CurrentWeatherCard(
                    place = place,
                    currentWeather = updatedWeather?.current,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, start = 28.dp, end = 28.dp)
                        .onGloballyPositioned { currentWeatherSize = it.size },
                    backgroundColor = colors.primary,
                    tint = colors.onPrimary,
                )
                IconButton(
                    onClick = { onEvent(HomeEvent.NavigateToMap) },
                    modifier = Modifier
                        .graphicsLayer {
                            val parentPaddingPx = 56.dp.toPx()
                            val currentWidthPx = currentWeatherSize.width.toFloat()
                            val currentHeightPx = currentWeatherSize.height.toFloat()

                            val hourlyWidthPx = hourlyWeatherSize.width.toFloat()
                            val widthDifference = hourlyWidthPx - currentWidthPx
                            val spacingOffset = maxOf(0f, minOf(widthDifference - parentPaddingPx, parentPaddingPx))
                            val distanceToMoveX = (widthDifference + spacingOffset) / 2f

                            val middlePaddingPxHalf = 15.dp.toPx()
                            val currentBottomPx = currentHeightPx - middlePaddingPxHalf
                            val hourlyTopPx = currentHeightPx + middlePaddingPxHalf

                            translationX = if (scrollState.value < currentBottomPx) {
                                0f
                            } else if (scrollState.value >= currentBottomPx && scrollState.value < hourlyTopPx) {
                                distanceToMoveX * (scrollState.value - currentBottomPx) / (middlePaddingPxHalf * 2)
                            } else {
                                distanceToMoveX
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
            visible = isHourlyWeatherVisible,
            modifier = Modifier
                .sizeIn(maxWidth = 500.dp)
                .padding(horizontal = 28.dp, vertical = 15.dp)
                .zIndex(1f),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            HourlyWeatherCard(
                hourlyWeathers = updatedWeather?.hourly ?: emptyList(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { hourlyWeatherSize = it.size },
                baseGradientColor = colors.secondary,
                tint = colors.onSecondary
            )
        }
        AnimatedVisibility(
            visible = isDailyWeatherVisible,
            modifier = Modifier
                .sizeIn(maxWidth = 500.dp)
                .padding(horizontal = 28.dp)
                .padding(bottom = 28.dp)
                .zIndex(0f),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            DailyWeatherCard(
                dailyWeathers = updatedWeather?.daily ?: emptyList(),
                modifier = Modifier.fillMaxWidth(),
                baseGradientColor = colors.secondary,
                tint = colors.onSecondary
            )
        }
    }
}