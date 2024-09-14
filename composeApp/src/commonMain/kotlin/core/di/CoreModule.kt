package core.di

import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.mobile
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    single { Geolocator.mobile() }
}