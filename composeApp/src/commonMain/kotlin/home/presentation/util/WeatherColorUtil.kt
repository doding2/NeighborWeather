package home.presentation.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
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
        WeatherType.CloudySunny,
        WeatherType.Sunny -> {
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
        WeatherType.CloudySunny,
        WeatherType.Sunny -> {
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
        WeatherType.CloudySunny,
        WeatherType.Sunny -> {
            if (darkTheme) sunnyNightSecondary else sunnyDaySecondary
        }
        WeatherType.Other, null -> Color.Transparent
    }
}