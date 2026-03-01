package weather.domain.repository

import core.domain.util.Error
import core.domain.util.Result
import dev.jordond.compass.Place
import kotlinx.coroutines.flow.Flow
import weather.domain.model.Neighbor
import weather.domain.model.Weather

interface WeatherRepository {
    suspend fun fetchRemoteWeather(
        place: Place,
        neighbor: Neighbor = Neighbor.ALL,
    ): Result<Weather, Error>

    suspend fun loadWeathers(
        place: Place,
        neighborWeights: Map<Neighbor, Double>,
        fetchFromRemote: Boolean = true,
    ): Flow<Result<Weather, Error>>

    suspend fun saveNeighborWeights(neighborWeights: Map<Neighbor, Double>)

    suspend fun loadNeighborWeights(): Map<Neighbor, Double>
}