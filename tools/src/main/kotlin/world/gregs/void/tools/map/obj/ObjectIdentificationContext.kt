package world.gregs.void.tools.map.obj

import world.gregs.void.ai.Context
import world.gregs.void.ai.Decision
import world.gregs.void.engine.entity.obj.GameObject
import world.gregs.void.engine.map.Tile

class ObjectIdentificationContext(
    val obj: GameObject,
    val availableTiles: Set<Tile>,
    val option: String
) : Context {
    val opt = option.replace("-", " ").toLowerCase()
    override var last: Decision? = null
}