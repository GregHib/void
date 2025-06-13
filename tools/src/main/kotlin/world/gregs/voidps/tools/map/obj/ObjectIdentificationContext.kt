package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

class ObjectIdentificationContext(
    val obj: GameObject,
    val availableTiles: Set<Tile>,
    option: String,
) {
    val opt = option.replace("-", " ").lowercase()
    var last: Triple<Double, *, *>? = null
}
