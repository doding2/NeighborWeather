package weather.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import weather.data.model.entity.CurrentWeatherEntity

@Dao
interface CurrentWeatherDao {

    @Upsert
    suspend fun upsertCurrentWeather(current: CurrentWeatherEntity)

    @Upsert
    suspend fun upsertCurrentWeatherList(currentList: List<CurrentWeatherEntity>)

    @Query("SELECT * FROM CurrentWeatherEntity " +
            "WHERE locationName = :locationName " +
            "AND neighbor = :neighbor " +
            "ORDER BY epochTime DESC LIMIT 1")
    suspend fun searchCurrentWeatherList(
        locationName: String,
        neighbor: String,
    ): List<CurrentWeatherEntity>

    @Query("SELECT * FROM CurrentWeatherEntity " +
            "WHERE locationName = :locationName " +
            "AND neighbor = :neighbor " +
            "ORDER BY epochTime DESC LIMIT 1")
    fun searchCurrentWeatherListFlow(
        locationName: String,
        neighbor: String,
    ): Flow<List<CurrentWeatherEntity>>

    @Query("DELETE FROM CurrentWeatherEntity " +
            "WHERE locationName = :locationName " +
            "AND epochTime < :now - 3600")
    suspend fun deletePastCurrentWeather(locationName: String, now: Long)    // 3600 seconds = 1 hour
}