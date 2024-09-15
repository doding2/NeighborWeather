package map.domain.util

import dev.jordond.compass.Place

actual fun List<Place>.getFirstDetailedPlace(): Place? {
    return filter {
        it.thoroughfare != null
    }.let { list ->
        list.firstOrNull { it.subLocality != null }
            ?: list.firstOrNull { it.locality != null }
    } ?: firstOrNull()
}

actual fun Place.toPlaceIdentifier(): String {
    return (if (isoCountryCode?.lowercase() == "kr") {
        subLocality?.let {
            buildString {
                append(it)
                thoroughfare?.let { append(" $thoroughfare") }
            }
        } ?: locality?.let {
            buildString {
                append(it)
                thoroughfare?.let { append(" $thoroughfare") }
            }
        }
        ?: subAdministrativeArea ?: administrativeArea
        ?: country ?: street
    } else {
        subLocality ?: locality
        ?: subAdministrativeArea ?: administrativeArea
        ?: country ?: street
    })?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: firstValue
}

actual fun Place.toPlaceAddress(): String = street?.takeIf { it.isNotEmpty() } ?: firstValue