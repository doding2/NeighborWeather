package weather.domain.repository

import core.util.Error
import core.util.Result
import dev.jordond.compass.Place
import kotlinx.coroutines.flow.Flow
import weather.domain.model.Neighbor
import weather.domain.model.Weather

interface WeatherRepository {
    suspend fun getWeathers(place: Place, targetToWeight: Map<Neighbor, Double>): Flow<Result<Weather, Error>>
}