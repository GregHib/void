package world.gregs.voidps.cache.encode

import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.engine.map.region.Region

/**
 * Adds collision for all blocked tiles except bridges
 */
class CollisionReader(private val collisions: Collisions) {

    fun read(region: Region, map: MapDefinition) {
        val x = region.tile.x
        val y = region.tile.y
        for (plane in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    if (localX.rem(8) == 0 && localY.rem(8) == 0) {
                        collisions.allocateIfAbsent(x + localX, y + localY, plane)
                    }
                    val blocked = map.getTile(localX, localY, plane).isTile(BLOCKED_TILE)
                    if (!blocked) {
                        continue
                    }
                    var height = plane
                    val bridge = map.getTile(localX, localY, 1).isTile(BRIDGE_TILE)
                    if (bridge) {
                        height--
                    }
                    if (height >= 0) {
                        collisions.add(x + localX, y + localY, height, FLOOR)
                    }
                }
            }
        }
    }

    companion object {
        public const val FLOOR: Int = 0x200000
        const val BLOCKED_TILE = 0x1
        const val BRIDGE_TILE = 0x2
    }
}
