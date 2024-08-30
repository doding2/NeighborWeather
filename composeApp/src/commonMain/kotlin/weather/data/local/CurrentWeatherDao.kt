package weather.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import weather.data.model.weather_entity.CurrentWeatherEntity

@Dao
interface CurrentWeatherDao {

    @Upsert
    suspend fun upsertCurrentWeather(current: CurrentWeatherEntity)

    @Upsert
    suspend fun upsertCurrentWeatherList(currentList: List<CurrentWeatherEntity>)

    @Query("SELECT * FROM CurrentWeatherEntity " +
            "WHERE latitude = :latitude AND longitude = :longitude " +
            "AND neighbor = :neighbor AND epochTime " +
            "ORDER BY epochTime DESC LIMIT 1")
    suspend fun searchCurrentWeatherList(
        latitude: Double,
        longitude: Double,
        neighbor: String,
    ): List<CurrentWeatherEntity>

    @Query("SELECT * FROM CurrentWeatherEntity " +
            "WHERE latitude = :latitude AND longitude = :longitude " +
            "AND neighbor = :neighbor AND epochTime " +
            "ORDER BY epochTime DESC LIMIT 1")
    fun searchCurrentWeatherListFlow(
        latitude: Double,
        longitude: Double,
        neighbor: String,
    ): Flow<List<CurrentWeatherEntity>>

}