package world.gregs.voidps.engine.entity.obj

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap

/**
 * Stores [GameObject]s by zone + group
 * Fast and low memory usage for a small (<100k) number of objects.
 */
class GameObjectHashMap : GameObjectMap {
    private val data = Int2IntOpenHashMap(EXPECTED_OBJECT_COUNT)

    override operator fun get(x: Int, y: Int, level: Int, group: Int): Int {
        return data.getOrDefault(index(x, y, level, group), -1)
    }

    override operator fun set(x: Int, y: Int, level: Int, group: Int, mask: Int) {
        data[index(x, y, level, group)] = mask
    }

    override fun add(x: Int, y: Int, level: Int, group: Int, mask: Int) {
        val currentFlags = data.getOrDefault(index(x, y, level, group), 0)
        this[x, y, level, group] = currentFlags or mask
    }

    override fun remove(x: Int, y: Int, level: Int, group: Int, mask: Int) {
        val currentFlags = this[x, y, level, group]
        this[x, y, level, group] = currentFlags and mask.inv()
    }

    override fun deallocateZone(zoneX: Int, zoneY: Int, level: Int) {
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                data.remove(index(zoneX + x, zoneY + y, level, ObjectGroup.WALL))
                data.remove(index(zoneX + x, zoneY + y, level, ObjectGroup.WALL_DECORATION))
                data.remove(index(zoneX + x, zoneY + y, level, ObjectGroup.INTERACTIVE))
                data.remove(index(zoneX + x, zoneY + y, level, ObjectGroup.GROUND_DECORATION))
            }
        }
    }

    override fun clear() {
        data.clear()
    }

    companion object {
        private const val EXPECTED_OBJECT_COUNT = 74_000
        private fun index(x: Int, y: Int, level: Int, group: Int): Int = level + (group shl 2) + (x shl 4) + (y shl 18)
    }
}