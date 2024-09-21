package weather.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import weather.data.model.entity.HourlyWeatherEntity

@Dao
interface HourlyWeatherDao {

    @Upsert
    suspend fun upsertHourlyWeather(hourly: HourlyWeatherEntity)

    @Upsert
    suspend fun upsertHourlyWeatherList(hourlyList: List<HourlyWeatherEntity>)

    @Query("SELECT * FROM HourlyWeatherEntity " +
            "WHERE locationName = :locationName " +
            "AND neighbor = :neighbor AND epochTime >= :now " +
            "ORDER BY epochTime ASC")
    suspend fun searchHourlyWeatherList(
        locationName: String,
        neighbor: String,
        now: Long
    ): List<HourlyWeatherEntity>

    @Query("SELECT * FROM HourlyWeatherEntity " +
            "WHERE locationName = :locationName " +
            "AND neighbor = :neighbor AND epochTime >= :now " +
            "ORDER BY epochTime ASC")
    fun searchHourlyWeatherListFlow(
        locationName: String,
        neighbor: String,
        now: Long
    ): Flow<List<HourlyWeatherEntity>>

    @Query("DELETE FROM HourlyWeatherEntity " +
            "WHERE locationName = :locationName " +
            "AND epochTime < :now - 3600")
    suspend fun deletePastHourlyWeather(locationName: String, now: Long)    // 3600 seconds = 1 hour
}