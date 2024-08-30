package weather.data.local.converters

import androidx.room.TypeConverter
import weather.domain.model.Neighbor

class NeighborConverter {
    @TypeConverter
    fun fromNeighbor(neighbor: Neighbor): String {
        return neighbor.toString()
    }

    @TypeConverter
    fun toNeighbor(value: String): Neighbor {
        return when (value.lowercase()) {
            "korea" -> Neighbor.Korea
            "japan" -> Neighbor.Japan
            "china" -> Neighbor.China
            "usa" -> Neighbor.USA
            "canada" -> Neighbor.Canada
            "australia" -> Neighbor.Australia
            "germany" -> Neighbor.Germany
            else -> Neighbor.ALL
        }
    }
}