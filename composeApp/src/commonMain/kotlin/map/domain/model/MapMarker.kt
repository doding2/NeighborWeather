package map.domain.model

data class MapMarker(
    val key: String = "",
    val position: Location = Location(0.0, 0.0),
    val title: String = "",
    val alpha: Float = 1.0f,
)