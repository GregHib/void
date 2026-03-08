package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.type.Tile

data class CanoeDefinition(
    override var stringId: String = "",
    var destination: Tile = Tile.EMPTY,
    var message: String = "",
    var sink: Tile = Tile.EMPTY,
    override var params: Map<Int, Any>? = null,
) : Parameterized {

    companion object {
        val EMPTY = CanoeDefinition()

        @Suppress("UNCHECKED_CAST")
        fun fromMap(name: String, map: MutableMap<Int, Any>): CanoeDefinition = CanoeDefinition(
            stringId = name,
            destination = Tile.fromArray(map.remove(Params.DESTINATION) as List<Int>),
            message = (map.remove(Params.MESSAGE) as? String) ?: EMPTY.message,
            sink = Tile.fromArray(map.remove(Params.SINK) as List<Int>),
            params = map,
        )
    }
}
