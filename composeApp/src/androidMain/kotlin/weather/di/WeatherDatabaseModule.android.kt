package weather.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import weather.data.local.WeatherDatabase

actual val weatherDatabaseModule: Module = module {
    single<WeatherDatabase> {
        val context: Context = getKoin().get()
        val dbFile = context.getDatabasePath("weather.db")
        Room.databaseBuilder<WeatherDatabase>(
            context = context.applicationContext,
            name = dbFile.name
        ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}