package weather.data.remote

import core.util.NetworkError
import core.util.Result
import core.util.toNetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerializationException
import weather.data.dto.neighbor_weather.NeighborWeatherDto
import weather.domain.model.Neighbor

class WeatherClient(
    private val httpClient: HttpClient,
) {
    suspend fun getKoreaWeather(
        latitude: Double,
        longitude: Double
    ): Result<NeighborWeatherDto, NetworkError> {
        val response = try {
            httpClient.get(
                urlString = "https://api.open-meteo.com/v1/forecast"
            ) {

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
                parameter("current", "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code")
                parameter("hourly", "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code")
                parameter("daily", "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,precipitation_sum,precipitation_hours")
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
}