package weather.data.util

import co.touchlab.kermit.Logger
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.select.Elements
import core.domain.util.Error
import core.domain.util.Result
import io.ktor.utils.io.charsets.MalformedInputException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import weather.data.model.dto.korean_weather_dto.KoreaCurrentWeatherDto
import weather.data.model.dto.korean_weather_dto.KoreaDailyWeatherDto
import weather.data.model.dto.korean_weather_dto.KoreaHourlyWeatherDto
import weather.data.model.dto.korean_weather_dto.KoreaWeatherDto

class KoreaWeatherParser {

    private val logger by lazy { Logger.withTag("KoreaWeatherParser") }

    suspend fun parseWeather(
        latitude: Double,
        longitude: Double,
        html: String,
        now: Instant
    ): Result<KoreaWeatherDto, KoreaWeatherParserException> {
        return withContext(Dispatchers.IO) {
            try {
                val doc = Ksoup.parse(html)

                val content = doc.select("section.sc_new.cs_weather_new._cs_weather")
                if (content.size == 0) {
                    return@withContext Result.Error(KoreaWeatherParserException.NO_OVERSEAS_SUPPORT)
                }

                val current = getCurrentWeather(content, now)
                val hourly = getHourlyWeather(content, now)
                val daily = getDailyWeather(content, now)

                val weather = KoreaWeatherDto(
                    latitude = latitude,
                    longitude = longitude,
                    current = current,
                    hourly = hourly,
                    daily = daily
                )

                Result.Success(
                    data = weather
                )
            } catch (e: MalformedInputException) {
                logger.e(e.stackTraceToString())
                Result.Error(KoreaWeatherParserException.MALFORMED_INPUT_EXCEPTION)
            } catch (e: Throwable) {
                logger.e(e.stackTraceToString())
                if (e is CancellationException) throw e
                Result.Error(KoreaWeatherParserException.HTML_PARSING_FAILED)
            }
        }
    }

    private fun getCurrentWeather(content: Elements, now: Instant): KoreaCurrentWeatherDto {
        val currentArea = content.select("div.api_subject_bx")
            .select("div.content_area")[0]
        val koreaDatetime = now.toLocalDateTime(TimeZone.of("Asia/Seoul"))
        val time = content.select("div.notice_area._related_info._info_layer_wrap > div.layer_pop._select_panel > p.desc")
            .textNodes()
            .firstOrNull()
            ?.text()
            ?.trim()
            ?.runCatching {
                val datetime = this.substringAfter("예보").substringBefore("업데이트").trim()
                val month = datetime.substringBefore(".").trim().toInt()
                val day = datetime.substringAfter(".").substringBefore(".").trim().toInt()
                val hour = datetime.substringAfter(". ").substringBefore(":").trim().toInt()
                val minute = datetime.substringAfter(":").trim().toInt()
                LocalDateTime(koreaDatetime.year, month, day, hour, minute, 0)
                    .toInstant(TimeZone.of("Asia/Seoul"))
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            }
            ?.getOrNull()
            ?: koreaDatetime
                .toInstant(TimeZone.of("Asia/Seoul"))
                .toLocalDateTime(TimeZone.currentSystemDefault())
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

        var apparentTemperature = temperature
        var precipitation = 0.0
        var humidity = 0.0
        var windDirection = "북풍"
        var windSpeed = 0.0

        summaryList.forEachIndexed { index, element ->
            if (element.select("dt.term").text().trim().startsWith("체감")) {
                apparentTemperature = element.select("dd.desc")
                    .text()
                    .trim()
                    .dropLast(1)
                    .toDouble()
            }
            else if (element.select("dt.term").text().trim().startsWith("강수")) {
                precipitation = element.select("dd.desc")
                    .text()
                    .trim()
                    .dropLast(2)
                    .toDouble()
            }
            else if (element.select("dt.term").text().trim().startsWith("습도")) {
                humidity = element.select("dd.desc")
                    .text()
                    .trim()
                    .dropLast(1)
                    .toDouble()
            }
            else if (index == summaryList.size - 1) {
                windDirection = element.select("dt.term")
                    .text()
                    .trim()
                windSpeed = element.select("dd.desc")
                    .text()
                    .trim()
                    .dropLast(3)
                    .toDouble()
            }
        }

        return KoreaCurrentWeatherDto(
            time = time,
            temperature = temperature,
            precipitation = precipitation,
            relativeHumidity = humidity,
            apparentTemperature = apparentTemperature,
            weather = weather,
            windSpeed = windSpeed,
            windDirection = windDirection
        )
    }

    private fun getHourlyWeather(content: Elements, now: Instant): KoreaHourlyWeatherDto {
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
                }).toLocalDateTime(TimeZone.of("Asia/Seoul"))

                LocalDateTime(datetime.year, datetime.monthNumber, datetime.dayOfMonth, hour, 0, 0)
                    .toInstant(TimeZone.of("Asia/Seoul"))
                    .toLocalDateTime(TimeZone.currentSystemDefault())
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

        val precipitationProbabilityList = mutableListOf<Double>()
        precipitationArea.select("div.climate_box > div.icon_wrap > ul > li")
            .mapIndexedTo(precipitationProbabilityList) { index, item ->
                val precipitation = precipitationList.getOrNull(index) ?: 0.0
                val probabilityByPrecipitation = if (precipitation == 0.0) 0.0 else 100.0

                item.select("em.value").text().trim()
                    .dropLast(1).toDoubleOrNull()
                    ?: probabilityByPrecipitation
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

        return KoreaHourlyWeatherDto(
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

    private fun getDailyWeather(content: Elements, now: Instant): KoreaDailyWeatherDto {
        val dailyArea = content.select("div.api_subject_bx._weekly_weather_wrap")
            .select("ul.week_list > li")

        val koreaDatetime = now.toLocalDateTime(TimeZone.of("Asia/Seoul"))
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
                LocalDateTime(koreaDatetime.year, month, day, 0, 0, 0)
                    .toInstant(TimeZone.of("Asia/Seoul"))
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
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

        return KoreaDailyWeatherDto(
            time = dateList,
            temperatureMax = temperatureMaxList,
            temperatureMin = temperatureMinList,
            precipitationProbabilityAM = precipitationProbabilityAMList,
            precipitationProbabilityPM = precipitationProbabilityPMList,
            weatherAM = weatherAMList,
            weatherPM = weatherPMList
        )
    }

    enum class KoreaWeatherParserException: Error {
        MALFORMED_INPUT_EXCEPTION,
        NO_OVERSEAS_SUPPORT,
        HTML_PARSING_FAILED;
    }
}