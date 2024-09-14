package weather.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import weather.data.model.entity.DailyWeatherEntity

@Dao
interface DailyWeatherDao {

    @Upsert
    suspend fun upsertDailyWeather(daily: DailyWeatherEntity)

    @Upsert
    suspend fun upsertDailyWeatherList(dailyList: List<DailyWeatherEntity>)

    @Query("SELECT * FROM DailyWeatherEntity " +
            "WHERE locationName = :locationName " +
            "AND neighbor = :neighbor AND epochTime >= :now - 86400 " +
            "ORDER BY epochTime ASC")
    suspend fun searchDailyWeatherList(
        locationName: String,
        neighbor: String,
        now: Long  // 86400 seconds = 1 day
    ): List<DailyWeatherEntity>

    @Query("SELECT * FROM DailyWeatherEntity " +
            "WHERE locationName = :locationName " +
            "AND neighbor = :neighbor AND epochTime >= :now - 86400 " +
            "ORDER BY epochTime ASC")
    fun searchDailyWeatherListFlow(
        locationName: String,
        neighbor: String,
        now: Long   // 86400 seconds = 1 day
    ): Flow<List<DailyWeatherEntity>>

}