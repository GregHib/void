package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class GameObjectCollisionRemove : GameObjectCollision() {

    override fun modifyTile(x: Int, y: Int, level: Int, block: Int, direction: Int) {
        var flags = Collisions.map.flags[Zone.tileIndex(x, y, level)]
        if (flags == null) {
            flags = Collisions.allocateIfAbsent(x, y, level)
        }
        flags[Tile.index(x, y)] = flags[Tile.index(x, y)] and CollisionFlags.inverse[direction or block]
    }
}
