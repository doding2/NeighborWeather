package weather.domain.repository

import core.util.Error
import core.util.Result
import weather.domain.model.Neighbor
import weather.domain.model.Weather

interface WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double, locationName: String, targetToWeight: Map<Neighbor, Double>): Result<Weather, Error>
}