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
                parameter("hourly", "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m")
                parameter("daily", "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,precipitation_sum,precipitation_hours,wind_speed_10m_max,wind_direction_10m_dominant")
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

//    suspend fun getKoreaCurrentWeather(
//        latitude: Double,
//        longitude: Double
//    ): Result<KoreaWeatherDto, NetworkError> {
//        val (nx, ny) = LocationGridConverter.convertToGrid(latitude, longitude)
//        val now = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Seoul"))
//        val baseDate = now.date.run { "$year${monthNumber.toString().padStart(2, '0')}${dayOfMonth.toString().padStart(2, '0')}" }
//        val baseTime = now.time.run { "${hour.toString().padStart(2, '0')}00" }
//
//        // 초단기 실황 (current)
//        // https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst
//        // 초단기 예보
//        // https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst
//        // 단기 예보
//        // https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst
//
//        val response = try {
//            httpClient.get(
//                urlString = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst"
//            ) {
//                parameter("serviceKey", BuildKonfig.KOREA_WEATHER_SERVICE_KEY)
//                parameter("pageNo", 1)
//                parameter("numOfRows", 1000)
//                parameter("dataType", "JSON")
//                parameter("base_date", baseDate)
//                parameter("base_time", baseTime)
//                parameter("nx", nx)
//                parameter("ny", ny)
//            }
//        } catch (e: UnresolvedAddressException) {
//            return Result.Error(NetworkError.NO_INTERNET)
//        } catch (e: SerializationException) {
//            return Result.Error(NetworkError.SERIALIZATION)
//        }
//
//        return response.status.value.let {
//            if (it in 200..299) {
//                Result.Success(response.body<KoreaWeatherDto>())
//            } else {
//                Result.Error(it.toNetworkError())
//            }
//        }
//    }
}