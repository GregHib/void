package rs.dusk.tools.map.obj

import rs.dusk.ai.Context
import rs.dusk.ai.Decision
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile

class ObjectIdentificationContext(
    val obj: GameObject,
    val availableTiles: Set<Tile>,
    val option: String
) : Context {
    val opt = option.replace("-", " ").toLowerCase()
    override var last: Decision? = null
}