package weather.domain.model

import dev.jordond.compass.Place

sealed interface Neighbor {
    data object Korea: Neighbor
    data object Japan: Neighbor
    data object China: Neighbor
    data object USA: Neighbor
    data object Canada: Neighbor
    data object Australia: Neighbor
    data object Germany: Neighbor
    data object ALL: Neighbor

    fun toModelsString(): String {
        return when (this) {
            is ALL -> "best_match"
            is Korea -> "best_match"
            is Japan -> "jma_seamless"
            is China -> "cma_grapes_global"
            is USA -> "gfs_seamless"
            is Canada -> "gem_seamless"
            is Australia -> "bom_access_global"
            is Germany -> "icon_seamless"
        }
    }

    fun toTextString(): String {
        return when(this) {
            ALL -> "All"
            Australia -> "Australia"
            Canada -> "Canada"
            China -> "China"
            Germany -> "Germany"
            Japan -> "Japan"
            Korea -> "Korea"
            USA -> "USA"
        }
    }
}

fun Place.toNeighbor(): Neighbor? {
    return when (this.isoCountryCode?.lowercase()) {
        "kr", "kor" -> return Neighbor.Korea
        "jp", "jpn" -> return Neighbor.Japan
        "cn", "chn" -> return Neighbor.China
        "us", "usa" -> return Neighbor.USA
        "ca", "can" -> return Neighbor.Canada
        "au", "aus" -> return Neighbor.Australia
        "de", "deu" -> return Neighbor.Germany
        else -> null
    }
}