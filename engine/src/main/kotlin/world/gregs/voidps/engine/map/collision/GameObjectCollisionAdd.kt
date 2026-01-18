package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.type.Tile

class GameObjectCollisionAdd : GameObjectCollision() {

    override fun modifyTile(x: Int, y: Int, level: Int, block: Int, direction: Int) {
        val flags = Collisions.allocateIfAbsent(x, y, level)
        flags[Tile.index(x, y)] = flags[Tile.index(x, y)] or CollisionFlags.blocked[direction or block]
    }
}
