package weather.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory
import weather.data.local.WeatherDatabase
import weather.data.local.instantiateImpl

actual val weatherDatabaseModule: Module = module {
    single<WeatherDatabase> {
        val dbFile = NSHomeDirectory() + "/weather.db"
        Room.databaseBuilder<WeatherDatabase>(
            name = dbFile,
            factory = { WeatherDatabase::class.instantiateImpl() }
        ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}