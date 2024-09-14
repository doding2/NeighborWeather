package map.domain.model

data class CameraPosition(
    val target: Location = Location(0.0, 0.0),
    val zoom: Float? = null
)