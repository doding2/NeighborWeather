package map.domain.model

data class CameraPositionLocationBounds(
    val coordinates: List<Location> = listOf(),
    val padding: Int = 0
)