package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

data class GameObjectOption(
    val option: String,
    val obj: GameObject,
    val tiles: Set<Tile>,
) {
    val opt = option.replace("-", " ").lowercase()
}
