package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.engine.map.collision.CollisionFlag.WATER
import world.gregs.voidps.engine.map.region.Region

/**
 * Adds collision for all blocked tiles except bridges
 */
class CollisionReader(val collisions: Collisions) {

    fun read(region: Region, map: MapDefinition) {
        val x = region.tile.x
        val y = region.tile.y
        for (plane in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    val blocked = map.getTile(localX, localY, plane).isTile(BLOCKED_TILE)
                    val bridge = map.getTile(localX, localY, 1).isTile(BRIDGE_TILE)
                    if (blocked && !bridge) {
                        collisions.add(x + localX, y + localY, plane, WATER)
                    }
                }
            }
        }
    }

    companion object {
        const val BLOCKED_TILE = 0x1
        const val BRIDGE_TILE = 0x2
    }
}
