package weather.data.model.dto.korean_weather_dto

import weather.data.model.dto.WeatherDto

data class KoreaWeatherDto(
    val latitude: Double,
    val longitude: Double,
    val current: KoreaCurrentWeatherDto,
    val hourly: KoreaHourlyWeatherDto,
    val daily: KoreaDailyWeatherDto
): WeatherDto