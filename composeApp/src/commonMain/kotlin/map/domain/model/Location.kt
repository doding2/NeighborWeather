package map.domain.model

import kotlin.math.sqrt

// https://github.com/realityexpander/ContactsComposeMultiplatform
data class Location(val latitude: Double = 0.0, val longitude: Double = 0.0)

fun Location.isWithinDistance(other: Location, distance: Double): Boolean {
    val latDiff = this.latitude - other.latitude
    val longDiff = this.longitude - other.longitude
    return sqrt(latDiff * latDiff + longDiff * longDiff) < distance
}