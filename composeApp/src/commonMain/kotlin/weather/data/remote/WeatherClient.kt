package weather.data.remote

import co.touchlab.kermit.Logger
import core.util.Error
import core.util.NetworkError
import core.util.Result
import core.util.toNetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerializationException
import weather.data.model.korean_weather.KoreaWeather
import weather.data.model.neighbor_weather.NeighborWeatherDto
import weather.data.util.KoreaWeatherParser
import weather.domain.model.Neighbor

class WeatherClient(
    private val httpClient: HttpClient,
    private val weatherParser: KoreaWeatherParser
) {
    private val logger by lazy { Logger.withTag("WeatherClient") }

    suspend fun getNeighborWeather(
        latitude: Double,
        longitude: Double,
        neighbor: Neighbor
    ): Result<NeighborWeatherDto, NetworkError> {
        val response = try {
            httpClient.get(
                urlString = "https://api.open-meteo.com/v1/forecast"
            ) {
                url {
                    parameter("latitude", latitude.toString())
                    parameter("longitude", longitude.toString())
                    parameter("current", "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m")
                    parameter("hourly", "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m,precipitation_probability")
                    parameter("daily", "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,precipitation_sum,precipitation_hours,wind_speed_10m_max,wind_direction_10m_dominant,precipitation_probability_max")
                    parameter("timezone", TimeZone.currentSystemDefault().toString())
                    parameter("past_days", "0")
                    parameter("forecast_days", "7")
                    parameter("models", neighbor.toModelsString())
                }
            }
        } catch (e: UnresolvedAddressException) {
            logger.e(e.stackTraceToString())
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            logger.e(e.stackTraceToString())
            return Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Throwable) {
            logger.e(e.stackTraceToString())
            if (e is CancellationException) throw e
            return Result.Error(NetworkError.UNKNOWN)
        }

        return response.status.value.let {
            if (it in 200..299) {
                try {
                    Result.Success(response.body<NeighborWeatherDto>())
                } catch (e: Throwable) {
                    logger.e(e.stackTraceToString())
                    if (e is CancellationException) throw e
                    Result.Error(NetworkError.UNKNOWN)
                }
            } else {
                Result.Error(it.toNetworkError())
            }
        }
    }
    suspend fun getKoreaWeather(
        latitude: Double,
        longitude: Double,
        locationName: String,
        now: Instant = Clock.System.now()
    ): Result<KoreaWeather, Error> {
        val url = "https://search.naver.com/search.naver"

        val response = try {
            httpClient.get(urlString = url) {
                url {
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.70 Safari/537.36")
                    parameter("query", "$locationName 날씨")
                }
            }
        } catch (e: UnresolvedAddressException) {
            logger.e(e.stackTraceToString())
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            logger.e(e.stackTraceToString())
            return Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Throwable) {
            logger.e(e.stackTraceToString())
            if (e is CancellationException) throw e
            return Result.Error(NetworkError.UNKNOWN)
        }

        val html = response.status.value.let {
            if (it in 200..299) {
                response.bodyAsText()
            } else {
                return Result.Error(it.toNetworkError())
            }
        }

        val weatherParsingResult = weatherParser.parseWeather(
            latitude = latitude,
            longitude = longitude,
            html = html,
            now = now
        )

        return weatherParsingResult
    }
}