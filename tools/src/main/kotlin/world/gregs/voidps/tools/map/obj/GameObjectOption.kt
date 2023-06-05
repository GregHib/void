package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.obj.GameMapObject
import world.gregs.voidps.engine.map.Tile

data class GameObjectOption(
    val option: String,
    val obj: GameMapObject,
    val tiles: Set<Tile>
) {
    val opt = option.replace("-", " ").lowercase()
}