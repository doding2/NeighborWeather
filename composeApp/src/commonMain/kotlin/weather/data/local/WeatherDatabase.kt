package weather.`data`.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import weather.data.local.converters.NeighborConverter
import weather.data.model.entity.CurrentWeatherEntity
import weather.data.model.entity.DailyWeatherEntity
import weather.data.model.entity.HourlyWeatherEntity

@Database(
    entities = [
        CurrentWeatherEntity::class,
        HourlyWeatherEntity::class,
        DailyWeatherEntity::class
    ],
    version = 2
)
@TypeConverters(value = [NeighborConverter::class])
@ConstructedBy(WeatherDatabaseConstructor::class)
abstract class WeatherDatabase: RoomDatabase() {
    abstract val currentWeatherDao: CurrentWeatherDao
    abstract val hourlyWeatherDao: HourlyWeatherDao
    abstract val dailyWeatherDao: DailyWeatherDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object WeatherDatabaseConstructor : RoomDatabaseConstructor<WeatherDatabase>