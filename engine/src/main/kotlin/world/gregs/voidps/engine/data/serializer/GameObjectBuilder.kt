package world.gregs.voidps.engine.data.serializer

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

internal data class GameObjectBuilder(
    val id: Int,
    var tile: ObjectTile,
    val type: Int,
    val rotation: Int,
) {
    data class ObjectTile(val x: Int, val y: Int, val plane: Int = 0)

    fun build() = GameObject(id, Tile(tile.x, tile.y, tile.plane), type, rotation)
}