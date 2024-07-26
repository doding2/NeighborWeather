package weather.data.remote

import core.util.NetworkError
import core.util.Result
import core.util.toNetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import org.koin.core.parameter.parametersOf
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
                parametersOf(
                    "latitude" to latitude.toString(),
                    "longitude" to longitude.toString(),
                    "current" to "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,cloud_cover",
                    "hourly" to "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code",
                    "daily" to "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,precipitation_sum,precipitation_hours",
                    "timezone" to "Asia/Tokyo",
                    "past_days" to "0",
                    "forecast_days" to "7"
                )
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
                parametersOf(
                    "latitude" to latitude.toString(),
                    "longitude" to longitude.toString(),
                    "current" to "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,cloud_cover",
                    "hourly" to "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code",
                    "daily" to "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,precipitation_sum,precipitation_hours",
                    "timezone" to "Asia/Tokyo", // time zone 바꾸기
                    "past_days" to "0",
                    "forecast_days" to "7",
                    "models" to neighbor.toModelsString()
                )
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