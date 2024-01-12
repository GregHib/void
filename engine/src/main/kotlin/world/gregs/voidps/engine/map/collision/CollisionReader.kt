package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapTile
import world.gregs.voidps.type.Region

/**
 * Adds collision for all blocked tiles except bridges
 */
class CollisionReader(private val collisions: Collisions) {

    fun read(region: Region, map: MapDefinition) {
        val x = region.tile.x
        val y = region.tile.y
        read(map.tiles, x, y)
    }

    fun read(tiles: LongArray, x: Int, y: Int) {
        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    addCollision(localX, localY, x, y, level, tiles)
                }
            }
        }
    }

    private fun addCollision(localX: Int, localY: Int, x: Int, y: Int, level: Int, tiles: LongArray) {
        if (localX.rem(8) == 0 && localY.rem(8) == 0) {
            collisions.allocateIfAbsent(x + localX, y + localY, level)
        }
        val blocked = isTile(tiles, localX, localY, level, BLOCKED_TILE)
        if (!blocked) {
            return
        }
        var height = level
        val bridge = isTile(tiles, localX, localY, 1, BRIDGE_TILE)
        if (bridge) {
            height--
        }
        if (height >= 0) {
            collisions.add(x + localX, y + localY, height, CollisionFlag.FLOOR)
        }
    }

    /**
     * TODO only apply collision in zone
     */
    fun read(tiles: LongArray, x: Int, y: Int, zoneRotation: Int) {
        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    val rotX = rotateX(localX, localY, zoneRotation)
                    val rotY = rotateY(localX, localY, zoneRotation)
                    addCollision(rotX, rotY, x, y, level, tiles)
                }
            }
        }
    }

    companion object {
        internal const val BLOCKED_TILE = 0x1
        internal const val BRIDGE_TILE = 0x2

        private fun isTile(tiles: LongArray, localX: Int, localY: Int, level: Int, flag: Int): Boolean {
            return MapTile.settings(tiles[MapDefinition.index(localX, localY, level)]) and flag == flag
        }

        private fun rotateX(x: Int, y: Int, rotation: Int): Int {
            return (if (rotation == 1) y else if (rotation == 2) 7 - x else if (rotation == 3) 7 - y else x) and 0x7
        }

        private fun rotateY(x: Int, y: Int, rotation: Int): Int {
            return (if (rotation == 1) 7 - x else if (rotation == 2) 7 - y else if (rotation == 3) x else y) and 0x7
        }
    }
}
