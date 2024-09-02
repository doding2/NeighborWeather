package weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1
)
@TypeConverters(value = [NeighborConverter::class])
//@ConstructedBy(WeatherDatabaseConstructor::class)
abstract class WeatherDatabase: RoomDatabase(), DB {
    abstract val currentWeatherDao: CurrentWeatherDao
    abstract val hourlyWeatherDao: HourlyWeatherDao
    abstract val dailyWeatherDao: DailyWeatherDao

    override fun clearAllTables() {
        super.clearAllTables()
    }
}

// FIXME: Added a hack to resolve below issue:
// class 'WeatherDatabase_Impl' is not abstract and does not implement abstract base class member 'clearAllTables'.
interface DB {
    fun clearAllTables() {}
}

//// The Room compiler generates the `actual` implementations.
//@Suppress("NO_ACTUAL_FOR_EXPECT")
//expect object WeatherDatabaseConstructor : RoomDatabaseConstructor<WeatherDatabase>
