package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Much like [Collisions] this stores [GameObject]s by zone + group
 */
class GameObjectMap {
    private val flags: Array<IntArray?> = arrayOfNulls(TOTAL_ZONE_COUNT)

    operator fun get(absoluteX: Int, absoluteY: Int, level: Int, group: Int): Int {
        val zoneIndex = zoneIndex(absoluteX, absoluteY, level)
        val tileIndex = tileIndex(absoluteX, absoluteY, group)
        return flags[zoneIndex]?.get(tileIndex) ?: -1
    }

    operator fun set(absoluteX: Int, absoluteY: Int, level: Int, group: Int, mask: Int) {
        val tiles = flags[zoneIndex(absoluteX, absoluteY, level)]
            ?: allocateIfAbsent(absoluteX, absoluteY, level)
        tiles[tileIndex(absoluteX, absoluteY, group)] = mask
    }

    fun add(absoluteX: Int, absoluteY: Int, level: Int, group: Int, mask: Int) {
        val zoneIndex = zoneIndex(absoluteX, absoluteY, level)
        val tileIndex = tileIndex(absoluteX, absoluteY, group)
        val currentFlags = flags[zoneIndex]?.get(tileIndex) ?: 0
        this[absoluteX, absoluteY, level, group] = currentFlags or mask
    }

    fun remove(absoluteX: Int, absoluteY: Int, level: Int, group: Int, mask: Int) {
        val currentFlags = this[absoluteX, absoluteY, level, group]
        this[absoluteX, absoluteY, level, group] = currentFlags and mask.inv()
    }

    fun allocateIfAbsent(absoluteX: Int, absoluteY: Int, level: Int): IntArray {
        val zoneIndex = zoneIndex(absoluteX, absoluteY, level)
        val existingFlags = flags[zoneIndex]
        if (existingFlags != null) return existingFlags
        val tileFlags = IntArray(ZONE_TILE_COUNT)
        flags[zoneIndex] = tileFlags
        return tileFlags
    }

    fun deallocateIfPresent(absoluteX: Int, absoluteY: Int, level: Int) {
        flags[zoneIndex(absoluteX, absoluteY, level)] = null
    }

    fun clear() {
        flags.fill(null)
    }

    companion object {
        private const val TOTAL_ZONE_COUNT: Int = 2048 * 2048 * 4
        private const val ZONE_TILE_COUNT: Int = 8 * 8 * 4

        private fun tileIndex(x: Int, y: Int, group: Int): Int = (x and 0x7) or ((y and 0x7) shl 3) or ((group and 0x7) shl 6)

        private fun zoneIndex(x: Int, y: Int, level: Int): Int = ((x shr 3) and 0x7FF) or
                (((y shr 3) and 0x7FF) shl 11) or ((level and 0x3) shl 22)
    }
}