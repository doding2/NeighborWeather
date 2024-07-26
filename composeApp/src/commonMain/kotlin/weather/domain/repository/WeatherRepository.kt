package weather.domain.repository

import core.util.NetworkError
import core.util.Result
import weather.domain.model.Weather

interface WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double): Result<Weather, NetworkError>
}