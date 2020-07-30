package rs.dusk.engine.map.collision

import rs.dusk.engine.map.collision.CollisionFlag.FLOOR
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.tile.BLOCKED_TILE
import rs.dusk.engine.map.region.tile.BRIDGE_TILE
import rs.dusk.engine.map.region.tile.TileSettings
import rs.dusk.engine.map.region.tile.isTile

/**
 * Adds collision for all blocked tiles except bridges
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class CollisionReader(val collisions: Collisions) {

    fun read(region: Region, settings: TileSettings) {
        val x = region.tile.x
        val y = region.tile.y
        for (plane in settings.indices) {
            for (localX in settings[plane].indices) {
                for (localY in settings[plane][localX].indices) {
                    val blocked = settings.isTile(plane, localX, localY,
                        BLOCKED_TILE
                    )
                    val bridge = settings.isTile(1, localX, localY,
                        BRIDGE_TILE
                    )
                    if (blocked && !bridge) {
                        collisions.add(x + localX, y + localY, plane, FLOOR)
                    }
                }
            }
        }
    }
}
