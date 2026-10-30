package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class GameObjectCollisionRemove : GameObjectCollision() {

    override fun modifyTile(x: Int, y: Int, level: Int, block: Int, direction: Int) {
        // An unallocated zone is fully blocked, allocating one here would make it all walkable
        val flags = Collisions.map.flags[Zone.tileIndex(x, y, level)] ?: return
        flags[Tile.index(x, y)] = flags[Tile.index(x, y)] and CollisionFlags.inverse[direction or block]
    }
}
