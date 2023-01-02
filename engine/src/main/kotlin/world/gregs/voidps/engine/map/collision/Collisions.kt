package world.gregs.voidps.engine.map.collision

import org.rsmod.pathfinder.AbsoluteCoords
import org.rsmod.pathfinder.ZoneCoords
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.RegionPlane

@Suppress("NOTHING_TO_INLINE")
class Collisions(
    /**
     * A two-dimensional array to carry all the flags of the game, including instances.
     */
    val data: Array<IntArray?> = arrayOfNulls(TOTAL_ZONES)
) {

    /**
     * Destroys the flags array for the zone at [zoneCoords].
     */
    inline fun alloc(zoneCoords: ZoneCoords): IntArray {
        val packed = zoneCoords.packedCoords
        val current = data[packed]
        if (current != null) return current
        val new = IntArray(ZONE_SIZE)
        data[packed] = new
        return new
    }

    /**
     * Destroys the flags array for the zone at [zoneCoords].
     * It should be noted that [zoneCoords] are not absolute.
     * To convert from absolute coordinates to zone coordinates, divide the x and y values
     * each by 8(the size of one zone).
     * Example:
     * Converting absolute coordinates [3251, 9422, 1] to [zoneCoords] produces [406, 1177, 1].
     */
    inline fun destroy(zoneCoords: ZoneCoords) {
        data[zoneCoords.packedCoords] = null
    }

    /**
     * Gets the flag at the absolute coordinates [x, y, z], returning the [default] if the zone is not allocated.
     */
    inline operator fun get(x: Int, y: Int, level: Int, default: Int = 0): Int {
        val zoneCoords = ZoneCoords(x shr 3, y shr 3, level)
        val array = data[zoneCoords.packedCoords] ?: return default
        return array[zoneLocal(x, y)]
    }

    /**
     * Sets the flag at the absolute coordinates [x, y, z] to [flag].
     */
    inline operator fun set(x: Int, y: Int, level: Int, flag: Int) {
        alloc(ZoneCoords(x shr 3, y shr 3, level))[zoneLocal(x, y)] = flag
    }

    /**
     * Adds the [flag] bits to the existing flag at the absolute coordinates [x, y, z].
     */
    inline fun add(x: Int, y: Int, level: Int, flag: Int) {
        val flags = alloc(ZoneCoords(x shr 3, y shr 3, level))
        val index = zoneLocal(x, y)
        val cur = flags[index]
        flags[index] = cur or flag
    }

    /**
     * Removes the [flag] bits from the existing flag at the absolute coordinates [x, y, z].
     */
    inline fun remove(x: Int, y: Int, level: Int, flag: Int) {
        val flags = alloc(ZoneCoords(x shr 3, y shr 3, level))
        val index = zoneLocal(x, y)
        val cur = flags[index]
        flags[index] = cur and flag.inv()
    }

    /**
     * Gets the flag at the absolute coordinates, returning the [default] if the zone is not allocated.
     */
    inline operator fun get(absoluteCoords: AbsoluteCoords, default: Int = 0): Int {
        return get(absoluteCoords.x, absoluteCoords.y, absoluteCoords.z, default)
    }

    /**
     * Sets the flag at the absolute coordinates to [flag].
     */
    inline operator fun set(absoluteCoords: AbsoluteCoords, flag: Int) {
        set(absoluteCoords.x, absoluteCoords.y, absoluteCoords.z, flag)
    }

    /**
     * Adds the [flag] bits to the existing flag at the absolute coordinates.
     */
    inline fun add(absoluteCoords: AbsoluteCoords, flag: Int) {
        add(absoluteCoords.x, absoluteCoords.y, absoluteCoords.z, flag)
    }

    /**
     * Removes the [flag] bits from the existing flag at the absolute coordinates.
     */
    inline fun remove(absoluteCoords: AbsoluteCoords, flag: Int) {
        remove(absoluteCoords.x, absoluteCoords.y, absoluteCoords.z, flag)
    }

    inline fun zoneLocal(x: Int, y: Int): Int = (x and 0x7) or ((y and 0x7) shl 3)

    companion object {
        const val TOTAL_ZONES: Int = 2048 * 2048 * 4
        const val ZONE_SIZE: Int = 8 * 8
    }

    fun check(x: Int, y: Int, plane: Int, flag: Int): Boolean {
        return get(x, y, plane) and flag != 0
    }

    fun check(tile: Tile, flag: Int) = check(tile.x, tile.y, tile.plane, flag)

    fun add(char: Character) {
        for (x in 0 until char.size.width) {
            for (y in 0 until char.size.height) {
                add(char.tile.x + x, char.tile.y + y, char.tile.plane, entity(char))
            }
        }
    }

    fun remove(char: Character) {
        for (x in 0 until char.size.width) {
            for (y in 0 until char.size.height) {
                remove(char.tile.x + x, char.tile.y + y, char.tile.plane, entity(char))
            }
        }
    }

    fun move(character: Character, from: Tile, to: Tile) {
        for (x in 0 until character.size.width) {
            for (y in 0 until character.size.height) {
                remove(from.x + x, from.y + y, from.plane, entity(character))
            }
        }
        for (x in 0 until character.size.width) {
            for (y in 0 until character.size.height) {
                add(to.x + x, to.y + y, to.plane, entity(character))
            }
        }
    }

    /**
     * Note:
     *  Only suitable for copying of tile collisions, object definitions flags would require transformations
     *  Could accidentally copy collisions of characters active in [from]
     */
    fun copy(from: Chunk, to: Chunk, rotation: Int) {
//        val array = data[from.regionPlane.id]?.clone() ?: return
//        for (x in 0 until 8) {
//            for (y in 0 until 8) {
//                val toX = if (rotation == 1) y else if (rotation == 2) 7 - x else if (rotation == 3) 7 - y else x
//                val toY = if (rotation == 1) 7 - x else if (rotation == 2) 7 - y else if (rotation == 3) x else y
//                val value = CollisionFlag.rotate(array[index(from.tile.x + x, from.tile.y + y)], rotation)
//                set(to.tile.x + toX, to.tile.y + toY, to.plane, value)
//            }
//        }
    }

    fun clear(region: RegionPlane) {
//        data[region.id]?.fill(0)
    }

    private fun entity(character: Character): Int = if (character is Player) org.rsmod.pathfinder.flag.CollisionFlag.BLOCK_PLAYERS else (org.rsmod.pathfinder.flag.CollisionFlag.BLOCK_NPCS or if (character["solid", false]) org.rsmod.pathfinder.flag.CollisionFlag.FLOOR else 0)

}