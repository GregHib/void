package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class GameObjectCollisionAdd(
    private val collisions: Collisions
) : GameObjectCollision() {

    override fun modifyTile(x: Int, y: Int, level: Int, block: Int, direction: Int) {
        val flags = collisions.allocateIfAbsent(x, y, level)
        flags[Tile.index(x, y)] = flags[Tile.index(x, y)] or CollisionFlags.blocked[direction or block]
    }
}