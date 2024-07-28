package weather.data.remote

import core.util.CommonError
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
import weather.data.util.NaverWeatherParser
import weather.domain.model.Neighbor

class WeatherClient(
    private val httpClient: HttpClient,
    private val weatherParser: NaverWeatherParser
) {
    suspend fun getNeighborWeather(
        latitude: Double,
        longitude: Double,
        neighbor: Neighbor
    ): Result<NeighborWeatherDto, NetworkError> {
        val response = try {
            httpClient.get(
                urlString = "https://api.open-meteo.com/v1/forecast"
            ) {
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
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return response.status.value.let {
            if (it in 200..299) {
                Result.Success(response.body<NeighborWeatherDto>())
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
        val url = "https://search.naver.com/search.naver?&query=${locationName.replace(" ", "+")}+날씨"

        val response = try {
            httpClient.get(url) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.70 Safari/537.36")
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        val html = response.status.value.let {
            if (it in 200..299) {
                response.bodyAsText()
            } else {
                return Result.Error(it.toNetworkError())
            }
        }

        val weather = try {
            weatherParser.parseWeather(latitude, longitude, html, now)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            return Result.Error(CommonError.HTML_PARSING_FAILED)
        }

        return Result.Success(weather)
    }
}