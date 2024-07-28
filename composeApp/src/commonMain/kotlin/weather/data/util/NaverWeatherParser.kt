package weather.data.util

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.select.Elements
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import weather.data.model.korean_weather.KoreaCurrentWeather
import weather.data.model.korean_weather.KoreaDailyWeather
import weather.data.model.korean_weather.KoreaHourlyWeather
import weather.data.model.korean_weather.KoreaWeather

class NaverWeatherParser {
    fun parseWeather(latitude: Double, longitude: Double, html: String): KoreaWeather {
        val content = Ksoup.parse(html)
            .select("section.sc_new.cs_weather_new._cs_weather")

        val current = getCurrentWeather(content)
        val hourly = getHourlyWeather(content)
        val daily = getDailyWeather(content)

        return KoreaWeather(
            latitude = latitude,
            longitude = longitude,
            current = current,
            hourly = hourly,
            daily = daily
        )
    }

    private fun getCurrentWeather(content: Elements): KoreaCurrentWeather {
        val currentArea = content.select("div.api_subject_bx")
            .select("div.content_area")[0]
        val temperature = currentArea.select("div.temperature_text")
            .select("strong")
            .textNodes()[0]
            .text()
            .toDouble()
        val weather = currentArea.select("div.weather_main")
            .select("span.blind")
            .text()
        val summaryList = currentArea.select("div.temperature_info")
            .select("div.sort")
        val apparentTemperature = summaryList[0]
            .select("dd.desc")
            .text()
            .dropLast(1)
            .toDouble()
        val humidity = summaryList[1]
            .select("dd.desc")
            .text()
            .dropLast(1)
            .toDouble()
        val windDirection = summaryList[2]
            .select("dt.term")
            .text()
        val windSpeed = summaryList[2]
            .select("dd.desc")
            .text()
            .dropLast(3)
            .toDouble()

        return KoreaCurrentWeather(
            time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            temperature = temperature,
            relativeHumidity = humidity,
            apparentTemperature = apparentTemperature,
            weather = weather,
            windSpeed = windSpeed,
            windDirection = windDirection
        )
    }

    private fun getHourlyWeather(content: Elements): KoreaHourlyWeather {
        val hourlyArea = content.select("div.api_subject_bx")
            .select("div.content_area")[1]
            .select("div.hourly_forecast._tab_content")
        val weatherArea = hourlyArea[0]
        val precipitationArea = hourlyArea[1]
        val windArea = hourlyArea[2]
        val humidityArea = hourlyArea[3]

        val weatherList = mutableListOf<String>()
        val temperatureList = mutableListOf<Double>()
        weatherArea.select("div.graph_inner._hourly_weather > ul > li").forEach {
            val weather = it.select("dd.weather_box").text()
            weatherList.add(weather)

            val temperature = it.select("dd.degree_point > div.inner > div.point_box > span.num")
                .textNodes()[0].text().toDouble()
            temperatureList.add(temperature)
        }

        val now = Clock.System.now()
        var isTomorrow = false
        var isDayAfterTomorrow = false
        var isThreeDaysFromToday = false
        val timeList = mutableListOf<LocalDateTime>()
        precipitationArea
            .select("div.climate_box > div.time_wrap > ul > li")
            .mapTo(timeList) {
                val hourStr = it.text().trim()
                val hour = if (hourStr.endsWith("시")) {
                    hourStr.dropLast(1).toInt()
                } else {
                    0
                }

                if (!isThreeDaysFromToday) {
                    isThreeDaysFromToday = hourStr.endsWith('.')
                }
                if (!isDayAfterTomorrow) {
                    isDayAfterTomorrow = hourStr == "모레"
                }
                if (!isTomorrow) {
                    isTomorrow = hourStr == "내일"
                }

                val datetime = (if (isThreeDaysFromToday) {
                    now.plus(3, DateTimeUnit.DAY, TimeZone.of("Asia/Seoul"))
                } else if (isDayAfterTomorrow) {
                    now.plus(2, DateTimeUnit.DAY, TimeZone.of("Asia/Seoul"))
                } else if (isTomorrow) {
                    now.plus(1, DateTimeUnit.DAY, TimeZone.of("Asia/Seoul"))
                } else {
                    now
                }).toLocalDateTime(TimeZone.currentSystemDefault())

                LocalDateTime(datetime.year, datetime.monthNumber, datetime.dayOfMonth, hour, 0, 0)
            }

        val precipitationProbabilityList = mutableListOf<Double>()
        precipitationArea.select("div.climate_box > div.icon_wrap > ul > li")
            .mapTo(precipitationProbabilityList) {
                it.select("em.value").text()
                    .dropLast(1).toDoubleOrNull() ?: 0.0
            }

        val precipitationList = mutableListOf<Double>()
        precipitationArea.select("div.climate_box > div.graph_wrap.rainfall > ul > li")
            .mapTo(precipitationList) {
                it.text().trim().let { text ->
                    if (text.firstOrNull()?.isDigit() == false) {
                        text.drop(1)
                    } else {
                        text
                    }
                }.toDoubleOrNull() ?: 0.0
            }

        val windDirectionList = mutableListOf<String>()
        windArea.select("div.climate_box > div.icon_wrap > ul > li")
            .mapTo(windDirectionList) {
                it.text()
            }

        val windSpeedList = mutableListOf<Double>()
        windArea.select("div.climate_box > div.graph_wrap > ul > li")
            .mapTo(windSpeedList) {
                it.text().toDoubleOrNull() ?: 0.0
            }

        val humidityList = mutableListOf<Double>()
        humidityArea.select("div.climate_box > div.graph_wrap > ul > li")
            .mapTo(humidityList) {
                it.text().toDoubleOrNull() ?: 0.0
            }

        return KoreaHourlyWeather(
            time = timeList,
            temperature = temperatureList,
            relativeHumidity = humidityList,
            precipitation = precipitationList,
            precipitationProbability = precipitationProbabilityList,
            weather = weatherList,
            windSpeed = windSpeedList,
            windDirection = windDirectionList
        )
    }

    private fun getDailyWeather(content: Elements): KoreaDailyWeather {
        val dailyArea = content.select("div.api_subject_bx._weekly_weather_wrap")
            .select("ul.week_list > li")

        val now = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Seoul"))
        val dateList = mutableListOf<LocalDate>()
        val temperatureMaxList = mutableListOf<Double>()
        val temperatureMinList = mutableListOf<Double>()
        val precipitationProbabilityAMList = mutableListOf<Double>()
        val precipitationProbabilityPMList = mutableListOf<Double>()
        val weatherAMList = mutableListOf<String>()
        val weatherPMList = mutableListOf<String>()
        dailyArea.forEach {
            val date = it.select("div.cell_date > span.date_inner > span.date").text().trim().run {
                val month = this.substringBefore('.').toInt()
                val day = this.substringAfter('.').substringBefore('.').toInt()
                LocalDate(now.year, month, day)
            }
            dateList.add(date)

            val am = it.select("div.cell_weather > span.weather_inner")[0]
            val pbAM = am.select("span.weather_left > span.rainfall").text()
                .dropLast(1).toDoubleOrNull() ?: 0.0
            val weatherAM = am.select("i").text()
            precipitationProbabilityAMList.add(pbAM)
            weatherAMList.add(weatherAM)

            val pm = it.select("div.cell_weather > span.weather_inner")[1]
            val pbPM = pm.select("span.weather_left > span.rainfall").text()
                .dropLast(1).toDoubleOrNull() ?: 0.0
            val weatherPM = pm.select("i").text()
            precipitationProbabilityPMList.add(pbPM)
            weatherPMList.add(weatherPM)

            val temperatureMin = it.select("div.cell_temperature").select("span.lowest")
                .textNodes()[0].text().trim().dropLast(1).toDoubleOrNull() ?: 0.0
            val temperatureMax = it.select("div.cell_temperature").select("span.highest")
                .textNodes()[0].text().trim().dropLast(1).toDoubleOrNull() ?: 0.0
            temperatureMinList.add(temperatureMin)
            temperatureMaxList.add(temperatureMax)
        }

        return KoreaDailyWeather(
            time = dateList,
            temperatureMax = temperatureMaxList,
            temperatureMin = temperatureMinList,
            precipitationProbabilityAM = precipitationProbabilityAMList,
            precipitationProbabilityPM = precipitationProbabilityPMList,
            weatherAM = weatherAMList,
            weatherPM = weatherPMList
        )
    }
}