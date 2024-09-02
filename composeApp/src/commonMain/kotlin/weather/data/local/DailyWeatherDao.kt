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
            "WHERE latitude = :latitude AND longitude = :longitude " +
            "AND neighbor = :neighbor AND epochTime >= :now - 86400 " +
            "ORDER BY epochTime ASC")
    suspend fun searchDailyWeatherList(
        latitude: Double,
        longitude: Double,
        neighbor: String,
        now: Long  // 86400 seconds = 1 day
    ): List<DailyWeatherEntity>

    @Query("SELECT * FROM DailyWeatherEntity " +
            "WHERE latitude = :latitude AND longitude = :longitude " +
            "AND neighbor = :neighbor AND epochTime >= :now - 86400 " +
            "ORDER BY epochTime ASC")
    fun searchDailyWeatherListFlow(
        latitude: Double,
        longitude: Double,
        neighbor: String,
        now: Long   // 86400 seconds = 1 day
    ): Flow<List<DailyWeatherEntity>>

}