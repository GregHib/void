package world.gregs.void.tools.map.obj

import world.gregs.void.engine.entity.obj.GameObject
import world.gregs.void.engine.map.Tile

data class GameObjectOption(
    val option: String,
    val obj: GameObject,
    val tiles: Set<Tile>
) {
    val opt = option.replace("-", " ").toLowerCase()
}