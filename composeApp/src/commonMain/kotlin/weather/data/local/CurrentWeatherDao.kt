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

    @Query("SELECT * FROM CurrentWeatherEntity " +
            "WHERE latitude = :latitude AND longitude = :longitude " +
            "AND neighbor = :neighbor AND epochTime BETWEEN :min AND :now " +
            "ORDER BY epochTime ASC")
    suspend fun searchCurrentWeatherList(
        latitude: Double,
        longitude: Double,
        neighbor: String,
        now: Long,
        min: Long = now - 3600
    ): List<CurrentWeatherEntity>

    @Query("SELECT * FROM CurrentWeatherEntity " +
            "WHERE latitude = :latitude AND longitude = :longitude " +
            "AND neighbor = :neighbor AND epochTime BETWEEN :min AND :now " +
            "ORDER BY epochTime ASC")
    fun searchCurrentWeatherListFlow(
        latitude: Double,
        longitude: Double,
        neighbor: String,
        now: Long,
        min: Long = now - 3600
    ): Flow<List<CurrentWeatherEntity>>

}