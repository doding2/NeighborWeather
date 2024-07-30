package weather.domain.model

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
}