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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jordond.compass.Place
import home.presentation.HomeEvent
import kotlinx.coroutines.delay
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
    val isCurrentWeatherVisible by remember { derivedStateOf { updatedWeather != null } }
    var isHourlyWeatherVisible by remember { mutableStateOf(isCurrentWeatherVisible) }
    LaunchedEffect(isCurrentWeatherVisible) {
        delay(100)
        isHourlyWeatherVisible = isCurrentWeatherVisible
    }
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(
                visible = isCurrentWeatherVisible,
                modifier = Modifier.sizeIn(maxWidth = 360.dp),
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
            ) {
                Box {
                    CurrentWeatherCard(
                        place = place,
                        currentWeather = weather?.current,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp, bottom = 28.dp, start = 28.dp, end = 28.dp),
                        backgroundColor = colors.primary,
                        tint = colors.onPrimary
                    )
                    IconButton(
                        onClick = { onEvent(HomeEvent.NavigateToMap) },
                        modifier = Modifier
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
            Spacer(modifier = Modifier.weight(1f))
        }
        AnimatedVisibility(
            visible = isHourlyWeatherVisible,
            modifier = Modifier.sizeIn(maxWidth = 360.dp),
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        ) {
            HourlyWeatherCard(
                hourlyWeathers = weather?.hourly ?: emptyList(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                baseGradientColor = colors.secondary,
                tint = colors.onSecondary
            )
        }




    }
}