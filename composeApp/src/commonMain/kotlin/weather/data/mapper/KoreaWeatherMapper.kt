package weather.data.mapper

import weather.data.model.korean_weather.KoreaWeather
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import kotlin.math.max

fun KoreaWeather.toWeather(): Weather {
    val weatherMap = mapOf(
        "맑음" to 0,
        "비" to 65,
        "구름많음" to 2,
        "흐림" to 3,
        "흐리고 한때 비" to 61,
        "흐리고 비" to 63,
        "구름많고 한때 비 곳" to 61,
        "구름많고 한때 비" to 61,
        "구름많고 비" to 63,
        "눈" to 75,
        "흐리고 한때 눈" to 71,
        "흐리고 눈" to 73,
        "구름많고 한때 눈 곳" to 71,
        "구름많고 한때 눈" to 71,
        "구름많고 눈" to 73,
    )
    val unitDegree = 22.5
    val windDirectionMap = mapOf(
        "북" to 0,
        "북북동" to 1,
        "북동" to 2,
        "동북동" to 3,
        "동" to 4,
        "동남동" to 5,
        "남동" to 6,
        "남남동" to 7,
        "남" to 8,
        "남남서" to 9,
        "남서" to 10,
        "서남서" to 11,
        "서" to 12,
        "서북서" to 13,
        "북서" to 14,
        "북북서" to 15
    ).mapValues { it.value * unitDegree }
    return Weather(
        latitude = latitude,
        longitude = longitude,
        neighbor = Neighbor.Korea,
        current = CurrentWeather(
            time = current.time,
            temperature = current.temperature,
            relativeHumidity = current.relativeHumidity,
            apparentTemperature = current.apparentTemperature,
            precipitation = hourly.precipitation[0],
            precipitationProbability = hourly.precipitationProbability[0],
            weatherCode = weatherMap[current.weather] ?: 0,
            windSpeed = current.windSpeed,
            windDirection = windDirectionMap[current.windDirection] ?: windDirectionMap.values.first()
        ),
        hourly = HourlyWeather(
            time = hourly.time,
            temperature = hourly.temperature,
            relativeHumidity = hourly.relativeHumidity,
            precipitation = hourly.precipitation,
            precipitationProbability = hourly.precipitationProbability,
            weatherCode = hourly.weather.map { weatherMap[it] ?: 0 },
            windSpeed = hourly.windSpeed,
            windDirection = hourly.windDirection.map { windDirectionMap[it] ?: windDirectionMap.values.first() }
        ),
        daily = DailyWeather(
            time = daily.time,
            temperatureMax = daily.temperatureMax,
            temperatureMin = daily.temperatureMin,
            precipitationProbability = daily.precipitationProbabilityAM
                .zip(daily.precipitationProbabilityPM, ::max),
            weatherCode = daily.weatherAM.zip(daily.weatherPM) { am, pm ->
                val amCode = weatherMap[am] ?: 0
                val pmCode = weatherMap[pm] ?: 0
                if (amCode > pmCode) amCode else pmCode
            },

        )
    )
}