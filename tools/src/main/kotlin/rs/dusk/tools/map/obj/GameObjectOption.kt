package rs.dusk.tools.map.obj

import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile

data class GameObjectOption(
    val option: String,
    val obj: GameObject,
    val tiles: Set<Tile>
) {
    val opt = option.replace("-", " ").toLowerCase()
}