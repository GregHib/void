package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class GameObjectCollisionRemove(
    private val collisions: Collisions,
) : GameObjectCollision() {

    override fun modifyTile(x: Int, y: Int, level: Int, block: Int, direction: Int) {
        var flags = collisions.flags[Zone.tileIndex(x, y, level)]
        if (flags == null) {
            flags = collisions.allocateIfAbsent(x, y, level)
        }
        flags[Tile.index(x, y)] = flags[Tile.index(x, y)] and CollisionFlags.inverse[direction or block]
    }
}
