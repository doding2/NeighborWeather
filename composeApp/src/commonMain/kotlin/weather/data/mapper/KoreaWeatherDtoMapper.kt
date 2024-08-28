package weather.data.mapper

import weather.data.remote.model.korean_weather.KoreaWeatherDto
import weather.domain.model.CurrentWeather
import weather.domain.model.DailyWeather
import weather.domain.model.HourlyWeather
import weather.domain.model.Neighbor
import weather.domain.model.Weather
import kotlin.math.max

fun KoreaWeatherDto.toWeather(): Weather {
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
        "북풍" to 0,
        "북북동풍" to 1,
        "북동풍" to 2,
        "동북동풍" to 3,
        "동풍" to 4,
        "동남동풍" to 5,
        "남동풍" to 6,
        "남남동풍" to 7,
        "남풍" to 8,
        "남남서풍" to 9,
        "남서풍" to 10,
        "서남서풍" to 11,
        "서풍" to 12,
        "서북서풍" to 13,
        "북서풍" to 14,
        "북북서풍" to 15
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
            precipitation = current.precipitation,
            precipitationProbability = hourly.precipitationProbability[0],
            weatherCode = weatherMap[current.weather] ?: 0,
            windSpeed = current.windSpeed,
            windDirection = windDirectionMap.let { map ->
                map[current.windDirection]
                    ?: map[hourly.windDirection.getOrElse(0) { map.keys.first() }]!!
            }
        ),
        hourly = List(hourly.time.size) { index ->
            HourlyWeather(
                time = hourly.time[index],
                temperature = hourly.temperature[index],
                relativeHumidity = hourly.relativeHumidity[index],
                precipitation = hourly.precipitation[index],
                precipitationProbability = hourly.precipitationProbability[index],
                weatherCode = weatherMap[hourly.weather[index]] ?: 0,
                windSpeed = hourly.windSpeed[index],
                windDirection = windDirectionMap[hourly.windDirection[index]] ?: windDirectionMap.values.first()
            )
        },
        daily = List(daily.time.size) { index ->
            DailyWeather(
                time = daily.time[index],
                temperatureMax = daily.temperatureMax[index],
                temperatureMin = daily.temperatureMin[index],
                precipitationProbability = max(
                    daily.precipitationProbabilityAM[index],
                    daily.precipitationProbabilityPM[index]
                ),
                weatherCode = max(
                    weatherMap[daily.weatherAM[index]] ?: 0,
                    weatherMap[daily.weatherPM[index]] ?: 0
                )
            )
        }
    )
}