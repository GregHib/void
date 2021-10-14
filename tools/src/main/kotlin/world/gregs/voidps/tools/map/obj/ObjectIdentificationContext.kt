package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

class ObjectIdentificationContext(
    val obj: GameObject,
    val availableTiles: Set<Tile>,
    val option: String
) {
    val opt = option.replace("-", " ").toLowerCase()
    var last: Triple<Double, *, *>? = null
}