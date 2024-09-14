package map.util

import dev.jordond.compass.Place


actual fun List<Place>.getFirstDetailedPlace(): Place? {
    return firstOrNull()
}

actual fun Place.toPlaceIdentifier(): String {
    return (if (isoCountryCode?.lowercase() == "kr") {
        locality?.let {
            buildString {
                append(it)
                if (subLocality != null) {
                    append(" $subLocality")
                } else {
                    thoroughfare?.let { append(" $thoroughfare") }
                }
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

actual fun Place.toPlaceAddress(): String {
    return buildString {
        country?.let { append("$it ") }
        locality?.let { append("$it ") }
        if (subLocality != thoroughfare && isoCountryCode?.lowercase() == "kr") {
            subLocality?.let { append("$it ") }
        }
        street?.let { append("$it ") }
    }.trim()
        .takeIf { it.isNotEmpty() }
        ?: firstValue
}