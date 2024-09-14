package weather.domain.model

sealed interface WeatherType {
    data object Sunny: WeatherType
    data object CloudySunny: WeatherType
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

fun Int.toWeatherType(): WeatherType {
    return when (this) {
        0, 1 -> WeatherType.Sunny
        2 -> WeatherType.CloudySunny
        3 -> WeatherType.Cloudy
        45, 48 -> WeatherType.Fog
        51, 53, 55 -> WeatherType.Drizzle
        56, 57 -> WeatherType.FreezingDrizzle
        61, 63, 65 -> WeatherType.Rainy
        66, 67 -> WeatherType.FreezingRain
        71, 73, 75, 77 -> WeatherType.Snowy
        80, 81, 82 -> WeatherType.RainShower
        85, 86 -> WeatherType.SnowShower
        95, 96, 99 -> WeatherType.Thunderstorm
        else -> WeatherType.Other
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