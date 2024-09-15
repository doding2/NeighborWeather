package core.presentation.util

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import core.presentation.ui.theme.cloudyDayOnPrimary
import core.presentation.ui.theme.cloudyDayPrimary
import core.presentation.ui.theme.cloudyDaySecondary
import core.presentation.ui.theme.cloudyNightOnPrimary
import core.presentation.ui.theme.cloudyNightPrimary
import core.presentation.ui.theme.cloudyNightSecondary
import core.presentation.ui.theme.rainyDayOnPrimary
import core.presentation.ui.theme.rainyDayPrimary
import core.presentation.ui.theme.rainyDaySecondary
import core.presentation.ui.theme.rainyNightOnPrimary
import core.presentation.ui.theme.rainyNightPrimary
import core.presentation.ui.theme.rainyNightSecondary
import core.presentation.ui.theme.snowyDayOnPrimary
import core.presentation.ui.theme.snowyDayPrimary
import core.presentation.ui.theme.snowyDaySecondary
import core.presentation.ui.theme.snowyNightOnPrimary
import core.presentation.ui.theme.snowyNightPrimary
import core.presentation.ui.theme.snowyNightSecondary
import core.presentation.ui.theme.sunnyDayOnPrimary
import core.presentation.ui.theme.sunnyDayPrimary
import core.presentation.ui.theme.sunnyDaySecondary
import core.presentation.ui.theme.sunnyNightOnPrimary
import core.presentation.ui.theme.sunnyNightPrimary
import core.presentation.ui.theme.sunnyNightSecondary
import weather.domain.model.WeatherType

@Composable
fun animateWeatherColors(weatherType: WeatherType?): Colors {
    val transition = updateTransition(targetState = weatherType)
    val primary by transition.animateColor(
        transitionSpec = {
            if (transition.currentState == null) tween(0)
            else tween(1000)
        },
        label = "primary",
        targetValueByState = { weatherPrimary(it) }
    )
    val onPrimary by transition.animateColor(
        transitionSpec = {
            if (transition.currentState == null) tween(0)
            else tween(1000)
        },
        label = "onPrimary",
        targetValueByState = { weatherOnPrimary(it) }
    )
    val secondary by transition.animateColor(
        transitionSpec = {
            if (transition.currentState == null) tween(0)
            else tween(1000)
        },
        label = "secondary",
        targetValueByState = { weatherSecondary(it) }
    )
    return MaterialTheme.colors.copy(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = Color.White
    )
}

@Composable
fun weatherPrimary(
    weatherType: WeatherType?,
    darkTheme: Boolean = isSystemInDarkTheme(),
): Color {
    return when (weatherType) {
        WeatherType.Fog,
        WeatherType.Cloudy -> {
            if (darkTheme) cloudyNightPrimary else cloudyDayPrimary
        }
        WeatherType.Drizzle,
        WeatherType.Rainy,
        WeatherType.RainShower,
        WeatherType.Thunderstorm -> {
            if (darkTheme) rainyNightPrimary else rainyDayPrimary
        }
        WeatherType.Snowy,
        WeatherType.SnowShower,
        WeatherType.FreezingDrizzle,
        WeatherType.FreezingRain -> {
            if (darkTheme) snowyNightPrimary else snowyDayPrimary
        }
        WeatherType.MainlyClear,
        WeatherType.Clear -> {
            if (darkTheme) sunnyNightPrimary else sunnyDayPrimary
        }
        WeatherType.Other, null -> Color.Transparent
    }
}

@Composable
fun weatherOnPrimary(
    weatherType: WeatherType?,
    darkTheme: Boolean = isSystemInDarkTheme(),
): Color {
    return when (weatherType) {
        WeatherType.Fog,
        WeatherType.Cloudy -> {
            if (darkTheme) cloudyNightOnPrimary else cloudyDayOnPrimary
        }
        WeatherType.Drizzle,
        WeatherType.Rainy,
        WeatherType.RainShower,
        WeatherType.Thunderstorm -> {
            if (darkTheme) rainyNightOnPrimary else rainyDayOnPrimary
        }
        WeatherType.Snowy,
        WeatherType.SnowShower,
        WeatherType.FreezingDrizzle,
        WeatherType.FreezingRain -> {
            if (darkTheme) snowyNightOnPrimary else snowyDayOnPrimary
        }
        WeatherType.MainlyClear,
        WeatherType.Clear -> {
            if (darkTheme) sunnyNightOnPrimary else sunnyDayOnPrimary
        }
        WeatherType.Other, null -> Color.Transparent
    }
}

@Composable
fun weatherSecondary(
    weatherType: WeatherType?,
    darkTheme: Boolean = isSystemInDarkTheme(),
): Color {
    return when (weatherType) {
        WeatherType.Fog,
        WeatherType.Cloudy -> {
            if (darkTheme) cloudyNightSecondary else cloudyDaySecondary
        }
        WeatherType.Drizzle,
        WeatherType.Rainy,
        WeatherType.RainShower,
        WeatherType.Thunderstorm -> {
            if (darkTheme) rainyNightSecondary else rainyDaySecondary
        }
        WeatherType.Snowy,
        WeatherType.SnowShower,
        WeatherType.FreezingDrizzle,
        WeatherType.FreezingRain -> {
            if (darkTheme) snowyNightSecondary else snowyDaySecondary
        }
        WeatherType.MainlyClear,
        WeatherType.Clear -> {
            if (darkTheme) sunnyNightSecondary else sunnyDaySecondary
        }
        WeatherType.Other, null -> Color.Transparent
    }
}