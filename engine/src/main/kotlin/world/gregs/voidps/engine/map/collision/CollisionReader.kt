package world.gregs.voidps.engine.map.collision

import org.rsmod.pathfinder.ZoneCoords
import org.rsmod.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.engine.map.region.Region

/**
 * Adds collision for all blocked tiles except bridges
 */
class CollisionReader(private val collisions: Collisions) {

    fun read(region: Region, map: MapDefinition) {
        for (plane in 0 until 4) {
            for (x in 0 until 8) {
                for (y in 0 until 8) {
                    collisions.alloc(ZoneCoords(region.tile.chunk.x + x, region.tile.chunk.y + y, plane))
                }
            }
        }
        val x = region.tile.x
        val y = region.tile.y
        for (plane in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
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
                        collisions.add(x + localX, y + localY, height, CollisionFlag.FLOOR)
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
