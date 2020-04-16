package rs.dusk.engine.map.collision

import rs.dusk.engine.map.collision.CollisionFlag.FLOOR
import rs.dusk.engine.model.Region
import rs.dusk.utility.inject

/**
 * Adds collision for all blocked tiles except bridges
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class CollisionLoader {

    val collisions: Collisions by inject()

    fun load(region: Region, settings: Array<Array<ByteArray>>) {
        val x = region.tile.x
        val y = region.tile.y
        for (plane in 0 until 3) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    val blocked = settings.isTile(plane, localX, localY, BLOCKED)
                    val bridge = settings.isTile(1, localX, localY, BRIDGE)
                    if (blocked && !bridge) {
                        collisions.add(x + localX, y + localY, plane, FLOOR)
                    }
                }
            }
        }
    }

    companion object {
        private fun Array<Array<ByteArray>>.isTile(plane: Int, localX: Int, localY: Int, flag: Int) =
            this[plane][localX][localY].toInt() and flag == flag

        private const val BLOCKED = 0x1
        private const val BRIDGE = 0x2
    }
}
