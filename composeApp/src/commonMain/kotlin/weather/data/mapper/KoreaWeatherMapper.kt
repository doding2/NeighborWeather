//package weather.data.mapper
//
//import kotlinx.datetime.LocalDateTime
//import kotlinx.datetime.format.FormatStringsInDatetimeFormats
//import kotlinx.datetime.format.byUnicodePattern
//import weather.data.dto.korean_weather.KoreaWeatherDto
//import weather.domain.model.CurrentWeather
//
//@OptIn(FormatStringsInDatetimeFormats::class)
//fun KoreaWeatherDto.toCurrentWeather(): CurrentWeather? {
//    val time = LocalDateTime.parse(
//        input = response.body.items.item.first().run { baseDate + baseTime },
//        format = LocalDateTime.Format {
//            byUnicodePattern("yyyyMMddHHmm")
//        }
//    )
//
//    val values = response.body.items.item.mapNotNull { item ->
//        item.obsrValue?.let {  value ->
//            item.category to value
//        }
//    }.toMap()
//
//    // SKY: 맑음(1), 구름많음(3), 흐림(4)
//    // PTY: 없음(0), 비(1), 비/눈(2), 눈(3), 빗방울(5), 빗방울눈날림(6), 눈날림(7)
//    // TODO: apparentTemperature, weatherCode 부분 수정 해야됨
//
//    val temperature = values["T1H"]?.toDoubleOrNull() ?: return null
//    val relativeHumidity = values["REH"]?.toDoubleOrNull() ?: return null
//    val apparentTemperature = temperature
//    val precipitation = values["RN1"]?.toDoubleOrNull() ?: 0.0
//    val weatherCode = values["PTY"]?.toIntOrNull() ?: return null
//    val windSpeed = values["WSD"]?.toDoubleOrNull() ?: return null
//    val windDirection = values["VEC"]?.toIntOrNull() ?: return null
//
//    return CurrentWeather(
//        time = time,
//        temperature = temperature,
//        relativeHumidity = relativeHumidity,
//        apparentTemperature = apparentTemperature,
//        precipitation = precipitation,
//        weatherCode = weatherCode,
//        windSpeed = windSpeed,
//        windDirection = windDirection
//    )
//}