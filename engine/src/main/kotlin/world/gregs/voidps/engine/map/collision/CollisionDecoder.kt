package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import java.io.File

/**
 * Adds collision for all blocked tiles except bridges
 */
class CollisionDecoder {

    /**
     * Decode [settings] region [x] [y] into [Collisions]
     */
    fun decode(settings: ByteArray, x: Int, y: Int) {
        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    if (localX.rem(8) == 0 && localY.rem(8) == 0) {
                        Collisions.allocateIfAbsent(x + localX, y + localY, level)
                    }
                    if (isTile(settings, localX, localY, level, ROOF_TILE)) {
                        Collisions.setUnsafe(x + localX, y + localY, level, CollisionFlag.ROOF)
                    }
                    if (!isTile(settings, localX, localY, level, BLOCKED_TILE)) {
                        continue
                    }
                    var height = level
                    if (isTile(settings, localX, localY, 1, BRIDGE_TILE)) {
                        if (--height < 0) {
                            continue
                        }
                    }
                    Collisions.setUnsafe(x + localX, y + localY, height, CollisionFlag.FLOOR)
                }
            }
        }
    }

    private fun Collisions.setUnsafe(x: Int, y: Int, level: Int, mask: Int) {
        val flags = map.flags[Zone.tileIndex(x, y, level)]!!
        val tile = Tile.index(x, y)
        flags[tile] = flags[tile] or mask
    }

    /**
     * Decode [from] Zone [settings] into [Collisions] [to] with applied [zoneRotation]
     */
    fun decode(settings: ByteArray, from: Zone, to: Zone, zoneRotation: Int) {
        val x = from.tile.x.rem(64)
        val y = from.tile.y.rem(64)
        val targetX = to.tile.x
        val targetY = to.tile.y
        for (level in 0 until 4) {
            Collisions.allocateIfAbsent(targetX, targetY, level)
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

                    if (isTile(settings, localX, localY, level, ROOF_TILE)) {
                        Collisions.setUnsafe(targetX + rotX, targetY + rotY, height, CollisionFlag.ROOF)
                    }
                    Collisions.setUnsafe(targetX + rotX, targetY + rotY, height, CollisionFlag.FLOOR)
                }
            }
        }
    }

    fun load(file: File): Int {
        val reader = ArrayReader(file.readBytes())
        val full = reader.readInt()
        for (i in 0 until full) {
            val index = reader.readInt()
            val array = IntArray(64)
            reader.readBytes(array)
            Collisions.map.flags[index] = array
        }
        val equals = reader.readInt()
        for (i in 0 until equals) {
            val index = reader.readInt()
            val type = reader.readInt()
            Collisions.map.flags[index] = IntArray(64) { type }
        }
        return full + equals
    }

    fun save(file: File) {
        val writer = BufferWriter(26_000_000)
        val full = mutableListOf<Int>()
        val equals = mutableListOf<Int>()
        val flags = Collisions.map.flags
        for (i in 0 until flags.size) {
            if (flags[i] == null) continue
            val first = flags[i]!![0]
            if (flags[i]!!.all { it == first }) {
                equals.add(i)
            } else {
                full.add(i)
            }
        }
        writer.writeInt(full.size)
        for (index in full) {
            writer.writeInt(index)
            writer.writeBytes(flags[index]!!)
        }
        writer.writeInt(equals.size)
        for (index in equals) {
            writer.writeInt(index)
            writer.writeInt(flags[index]!![0])
        }
        file.writeBytes(writer.toArray())
    }

    companion object {
        internal const val BLOCKED_TILE = 0x1
        internal const val BRIDGE_TILE = 0x2
        internal const val ROOF_TILE = 0x4

        private fun isTile(tiles: ByteArray, localX: Int, localY: Int, level: Int, flag: Int): Boolean = tiles[MapDefinition.index(localX, localY, level)].toInt() and flag == flag

        private fun rotateX(x: Int, y: Int, rotation: Int): Int = (
                when (rotation) {
                    1 -> y
                    2 -> 7 - x
                    3 -> 7 - y
                    else -> x
                }
                ) and 0x7

        private fun rotateY(x: Int, y: Int, rotation: Int): Int = (
                when (rotation) {
                    1 -> 7 - x
                    2 -> 7 - y
                    3 -> x
                    else -> y
                }
                ) and 0x7
    }
}
