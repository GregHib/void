package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.type.Zone

/**
 * Adds collision for all blocked tiles except bridges
 */
class CollisionDecoder(private val collisions: Collisions) {

    fun decode(tiles: ByteArray, x: Int, y: Int) {
        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    if (localX.rem(8) == 0 && localY.rem(8) == 0) {
                        collisions.allocateIfAbsent(x + localX, y + localY, level)
                    }
                    if (!isTile(tiles, localX, localY, level, BLOCKED_TILE)) {
                        continue
                    }
                    var height = level
                    if (isTile(tiles, localX, localY, 1, BRIDGE_TILE)) {
                        if (--height < 0) {
                            continue
                        }
                    }
                    collisions.setCol(x + localX, y + localY, height, CollisionFlag.FLOOR)
                }
            }
        }
    }

    private val indices = IntArray(16384)

    init {
        var i = 0
        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    indices[i++] = MapDefinition.index(localX, localY, level)
                }
            }
        }
    }

    private fun Collisions.setCol(x: Int, y: Int, level: Int, mask: Int) {
        val flags = allocateIfAbsent(x, y, level)
        val tile = tileIndex(x, y)
        flags[tile] = flags[tile] or mask
    }

    private fun zoneIndex(x: Int, z: Int, level: Int): Int = ((x shr 3) and 0x7FF) or
            (((z shr 3) and 0x7FF) shl 11) or ((level and 0x3) shl 22)

    private fun tileIndex(x: Int, z: Int): Int = (x and 0x7) or ((z and 0x7) shl 3)

    /**
     * Decode [from] Zone [settings] into [Collisions] [to] with applied [zoneRotation]
     */
    fun decode(settings: ByteArray, from: Zone, to: Zone, zoneRotation: Int) {
        val x = from.tile.x.rem(64)
        val y = from.tile.y.rem(64)
        val targetX = to.tile.x
        val targetY = to.tile.y
        for (level in 0 until 4) {
            collisions.allocateIfAbsent(targetX, targetY, level)
            for (localX in x until x + 8) {
                for (localY in y until y + 8) {
                    if (!isTile(settings, localX, localY, level, BLOCKED_TILE)) {
                        continue
                    }
                    var height = level
                    if (isTile(settings, localX, localY, 1, BRIDGE_TILE)) {
                        if (--height < 0) {
                            continue
                        }
                    }
                    val rotX = rotateX(localX, localY, zoneRotation)
                    val rotY = rotateY(localX, localY, zoneRotation)
                    val tiles = collisions.flags[zoneIndex(targetX + rotX, targetY + rotY, level)]!!
                    val tileIndex = tileIndex(targetX + rotX, targetY + rotY)
                    tiles[tileIndex] = tiles[tileIndex] or CollisionFlag.FLOOR
                }
            }
        }
    }

    companion object {
        internal const val BLOCKED_TILE = 0x1
        internal const val BRIDGE_TILE = 0x2

        private fun isTile(tiles: ByteArray, localX: Int, localY: Int, level: Int, flag: Int): Boolean {
            return tiles[MapDefinition.index(localX, localY, level)].toInt() and flag == flag
        }

        private fun rotateX(x: Int, y: Int, rotation: Int): Int {
            return (if (rotation == 1) y else if (rotation == 2) 7 - x else if (rotation == 3) 7 - y else x) and 0x7
        }

        private fun rotateY(x: Int, y: Int, rotation: Int): Int {
            return (if (rotation == 1) 7 - x else if (rotation == 2) 7 - y else if (rotation == 3) x else y) and 0x7
        }
    }
}
