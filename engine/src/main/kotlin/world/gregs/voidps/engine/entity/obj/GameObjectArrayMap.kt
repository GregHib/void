package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Much like [Collisions] this stores [GameObject]s by zone + group
 * Large memory footprint but faster when storing millions of objects.
 */
class GameObjectArrayMap : GameObjectMap {
    private val data: Array<IntArray?> = arrayOfNulls(TOTAL_ZONE_COUNT)

    override fun get(obj: GameObject) = get(obj.x, obj.y, obj.plane, ObjectGroup.group(obj.type))

    override operator fun get(x: Int, y: Int, level: Int, group: Int): Int {
        val zoneIndex = zoneIndex(x, y, level)
        val tileIndex = tileIndex(x, y, group)
        return data[zoneIndex]?.get(tileIndex) ?: -1
    }

    override operator fun set(zone: Int, tile: Int, mask: Int) {
        val tiles = data[zone] ?: return
        tiles[tile] = mask
    }

    override operator fun set(x: Int, y: Int, level: Int, group: Int, mask: Int) {
        val tiles = data[zoneIndex(x, y, level)] ?: getOrAllocate(x, y, level)
        tiles[tileIndex(x, y, group)] = mask
    }

    override fun add(obj: GameObject, mask: Int) {
        val x = obj.x
        val y = obj.y
        val level = obj.plane
        val group = ObjectGroup.group(obj.type)
        val zoneIndex = zoneIndex(x, y, level)
        val tileIndex = tileIndex(x, y, group)
        val currentFlags = data[zoneIndex]?.get(tileIndex) ?: 0
        this[x, y, level, group] = currentFlags or mask
    }

    override fun remove(obj: GameObject, mask: Int) {
        val x = obj.x
        val y = obj.y
        val level = obj.plane
        val group = ObjectGroup.group(obj.type)
        val currentFlags = this[x, y, level, group]
        this[x, y, level, group] = currentFlags and mask.inv()
    }

    override fun allocateIfAbsent(absoluteX: Int, absoluteY: Int, level: Int) {
        getOrAllocate(absoluteX, absoluteY, level)
    }

    private fun getOrAllocate(absoluteX: Int, absoluteY: Int, level: Int): IntArray {
        val zoneIndex = zoneIndex(absoluteX, absoluteY, level)
        val existingFlags = data[zoneIndex]
        if (existingFlags != null) return existingFlags
        val tileFlags = IntArray(ZONE_TILE_COUNT)
        data[zoneIndex] = tileFlags
        return tileFlags
    }

    override fun deallocateZone(zoneX: Int, zoneY: Int, level: Int) {
        data[zoneIndex(zoneX, zoneY, level)] = null
    }

    fun isZoneAllocated(absoluteX: Int, absoluteY: Int, level: Int): Boolean {
        return data[zoneIndex(absoluteX, absoluteY, level)] != null
    }

    override fun clear() {
        data.fill(null)
    }

    companion object {
        private const val TOTAL_ZONE_COUNT: Int = 2048 * 2048 * 4
        private const val ZONE_TILE_COUNT: Int = 8 * 8 * 4

        private fun tileIndex(x: Int, y: Int, group: Int): Int = (x and 0x7) or ((y and 0x7) shl 3) or ((group and 0x7) shl 6)

        private fun zoneIndex(x: Int, y: Int, level: Int): Int = ((x shr 3) and 0x7FF) or
                (((y shr 3) and 0x7FF) shl 11) or ((level and 0x3) shl 22)

        @JvmStatic
        fun main(args: Array<String>) {

            println(zoneIndex(20, 20, 0))
            println(tileIndex(4, 4, 2))
            println(zoneIndex(20, 21, 0))
            println(tileIndex(4, 5, 2))
        }
    }
}