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
            "WHERE latitude = :latitude AND longitude = :longitude " +
            "AND neighbor = :neighbor AND epochTime >= :now " +
            "ORDER BY epochTime ASC")
    suspend fun searchHourlyWeatherList(
        latitude: Double,
        longitude: Double,
        neighbor: String,
        now: Long
    ): List<HourlyWeatherEntity>

    @Query("SELECT * FROM HourlyWeatherEntity " +
            "WHERE latitude = :latitude AND longitude = :longitude " +
            "AND neighbor = :neighbor AND epochTime >= :now " +
            "ORDER BY epochTime ASC")
    fun searchHourlyWeatherListFlow(
        latitude: Double,
        longitude: Double,
        neighbor: String,
        now: Long
    ): Flow<List<HourlyWeatherEntity>>

}