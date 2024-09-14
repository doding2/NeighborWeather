package map.util

import dev.jordond.compass.Place

// TODO: Move this file to Location module later

expect fun List<Place>.getFirstDetailedPlace(): Place?

expect fun Place.toPlaceIdentifier(): String

expect fun Place.toPlaceAddress(): String