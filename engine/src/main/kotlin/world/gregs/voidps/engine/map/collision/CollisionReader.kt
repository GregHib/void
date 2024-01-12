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
                    if (localX.rem(8) == 0 && localY.rem(8) == 0) {
                        collisions.allocateIfAbsent(x + localX, y + localY, level)
                    }
                    val blocked = isTile(tiles, localX, localY, level, BLOCKED_TILE)
                    if (!blocked) {
                        continue
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
            }
        }
    }

    companion object {
        private const val BLOCKED_TILE = 0x1
        private const val BRIDGE_TILE = 0x2

        private fun isTile(tiles: LongArray, localX: Int, localY: Int, level: Int, flag: Int): Boolean {
            return MapTile.settings(tiles[MapDefinition.index(localX, localY, level)]) and flag == flag
        }
    }
}
