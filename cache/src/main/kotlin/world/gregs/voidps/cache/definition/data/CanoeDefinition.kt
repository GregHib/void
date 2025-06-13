package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.type.Tile

data class CanoeDefinition(
    override var stringId: String = "",
    var destination: Tile = Tile.EMPTY,
    var message: String = "",
    var sink: Tile = Tile.EMPTY,
    override var extras: Map<String, Any>? = null,
) : Extra {

    companion object {
        val EMPTY = CanoeDefinition()

        @Suppress("UNCHECKED_CAST")
        fun fromMap(name: String, map: MutableMap<String, Any>): CanoeDefinition = CanoeDefinition(
            stringId = name,
            destination = Tile.fromArray(map.remove("destination") as List<Int>),
            message = (map.remove("message") as? String) ?: EMPTY.message,
            sink = Tile.fromArray(map.remove("sink") as List<Int>),
            extras = map,
        )
    }
}
