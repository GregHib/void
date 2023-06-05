package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.obj.GameMapObject
import world.gregs.voidps.engine.map.Tile

class ObjectIdentificationContext(
    val obj: GameMapObject,
    val availableTiles: Set<Tile>,
    option: String
) {
    val opt = option.replace("-", " ").lowercase()
    var last: Triple<Double, *, *>? = null
}