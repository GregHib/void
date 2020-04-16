package rs.dusk.engine.map.collision

import rs.dusk.engine.map.BLOCKED_TILE
import rs.dusk.engine.map.BRIDGE_TILE
import rs.dusk.engine.map.TileSettings
import rs.dusk.engine.map.collision.CollisionFlag.FLOOR
import rs.dusk.engine.map.isTile
import rs.dusk.engine.model.Region
import rs.dusk.utility.inject

/**
 * Adds collision for all blocked tiles except bridges
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class CollisionLoader {

    val collisions: Collisions by inject()

    fun load(region: Region, settings: TileSettings) {
        val x = region.tile.x
        val y = region.tile.y
        for (plane in 0 until 3) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    val blocked = settings.isTile(plane, localX, localY, BLOCKED_TILE)
                    val bridge = settings.isTile(1, localX, localY, BRIDGE_TILE)
                    if (blocked && !bridge) {
                        collisions.add(x + localX, y + localY, plane, FLOOR)
                    }
                }
            }
        }
    }
}
