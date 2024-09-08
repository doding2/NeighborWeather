package map.domain.model

import dev.jordond.compass.Place
import kotlin.math.sqrt

// https://github.com/realityexpander/ContactsComposeMultiplatform
data class Location(val latitude: Double = 0.0, val longitude: Double = 0.0)

fun Location.isWithinDistance(other: Location, distance: Double): Boolean {
    val latDiff = this.latitude - other.latitude
    val longDiff = this.longitude - other.longitude
    return sqrt(latDiff * latDiff + longDiff * longDiff) < distance
}

fun Place.toLocationName(): String {
    return if (isoCountryCode?.lowercase() == "kr") {
        subLocality?.let { "$it $thoroughfare" }
            ?: locality?.let { "$it $thoroughfare" }
            ?: subAdministrativeArea ?: administrativeArea
            ?: country ?: street!!
    } else {
        subLocality ?: locality
            ?: subAdministrativeArea ?: administrativeArea
            ?: country ?: street!!
    }
}