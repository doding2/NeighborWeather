package weather.domain.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import weather.domain.model.WeatherType.Clear
import weather.domain.model.WeatherType.Cloudy
import weather.domain.model.WeatherType.Drizzle
import weather.domain.model.WeatherType.Fog
import weather.domain.model.WeatherType.FreezingDrizzle
import weather.domain.model.WeatherType.FreezingRain
import weather.domain.model.WeatherType.MainlyClear
import weather.domain.model.WeatherType.Other
import weather.domain.model.WeatherType.RainShower
import weather.domain.model.WeatherType.Rainy
import weather.domain.model.WeatherType.SnowShower
import weather.domain.model.WeatherType.Snowy
import weather.domain.model.WeatherType.Thunderstorm

sealed interface WeatherType {
    data object Clear: WeatherType
    data object MainlyClear: WeatherType
    data object Fog: WeatherType
    data object Cloudy: WeatherType
    data object Drizzle: WeatherType
    data object FreezingDrizzle: WeatherType
    data object Rainy: WeatherType
    data object RainShower: WeatherType
    data object FreezingRain: WeatherType
    data object Snowy: WeatherType
    data object SnowShower: WeatherType
    data object Thunderstorm: WeatherType
    data object Other: WeatherType
}

@Composable
fun WeatherType.toFormattedName(darkTheme: Boolean = isSystemInDarkTheme()): String {
    return when (this) {
        Clear -> if (darkTheme) "Clear" else "Sunny"
        MainlyClear -> if (darkTheme) "Mainly Clear" else "Mainly Sunny"
        Fog -> "Fog"
        Cloudy -> "Cloudy"
        Drizzle -> "Drizzle"
        FreezingDrizzle -> "Freezing Drizzle"
        Rainy -> "Rainy"
        RainShower -> "Rain Shower"
        FreezingRain -> "Freezing Rain"
        Snowy -> "Snowy"
        SnowShower -> "Snow Shower"
        Thunderstorm -> "Thunderstorm"
        Other -> "Other"
    }
}

fun Int.toWeatherType(): WeatherType {
    return when (this) {
        0, 1 -> Clear
        2 -> MainlyClear
        3 -> Cloudy
        45, 48 -> Fog
        51, 53, 55 -> Drizzle
        56, 57 -> FreezingDrizzle
        61, 63, 65 -> Rainy
        66, 67 -> FreezingRain
        71, 73, 75, 77 -> Snowy
        80, 81, 82 -> RainShower
        85, 86 -> SnowShower
        95, 96, 99 -> Thunderstorm
        else -> Other
    }
}

fun String.toWeatherCode(): Int? {
    val weatherMap = mapOf(
        "맑음" to 0,
        "대체로 화창" to 1,   // 아큐웨더 (overseas)
        "일부 화창" to 1,   // 아큐웨더 (overseas)
        "화창" to 0,   // 아큐웨더 (overseas)
        "구름" to 2,
        "구름조금" to 2,
        "비" to 63,
        "소나기" to 73,   // 아큐웨더 (overseas)
        "번개뇌우" to 95,   // 아큐웨더 (overseas)
        "일부 흐림/소나기" to 81,   // 아큐웨더 (overseas)
        "구름많음" to 2,
        "흐림" to 3,
        "흐리고 한때 비" to 61,
        "흐리고 가끔 비" to 61,
        "흐리고 한때 소나기" to 80,
        "흐리고 비" to 63,
        "구름많고 한때 비 곳" to 61,
        "구름많고 한때 비" to 61,
        "구름많고 비" to 63,
        "눈" to 73,
        "흐리고 한때 눈" to 71,
        "흐리고 가끔 눈" to 71,
        "흐리고 눈" to 73,
        "구름많고 한때 눈 곳" to 71,
        "구름많고 한때 눈" to 71,
        "구름많고 눈" to 73,
    )

    return weatherMap[this]
        ?: weatherMap.entries
            .reversed()
            .firstOrNull { this.contains(it.key) }
            ?.value
}