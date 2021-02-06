package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.ai.Context
import world.gregs.voidps.ai.Decision
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

class ObjectIdentificationContext(
    val obj: GameObject,
    val availableTiles: Set<Tile>,
    val option: String
) : Context {
    val opt = option.replace("-", " ").toLowerCase()
    override var last: Decision<*, *>? = null
}