package world.gregs.voidps.engine.entity.obj

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk

/**
 * Stores [GameObject]s by zone + group
 * Fast and low memory usage for a small (<100k) number of objects.
 */
class GameObjectHashMap : GameObjectMap {
    private val data = Int2IntOpenHashMap(EXPECTED_OBJECT_COUNT)

    override fun get(obj: GameObject): Int {
        return data.getOrDefault(index(obj), -1)
    }

    override operator fun get(x: Int, y: Int, level: Int, group: Int): Int {
        return data.getOrDefault(index(x, y, level, group), -1)
    }

    override fun set(zone: Int, tile: Int, mask: Int) {
        data[index(zone, tile)] = mask
    }

    override operator fun set(x: Int, y: Int, level: Int, group: Int, mask: Int) {
        data[index(x, y, level, group)] = mask
    }

    override fun add(obj: GameObject, mask: Int) {
        val index = index(obj)
        val currentFlags = data.getOrDefault(index, 0)
        data[index] = currentFlags or mask
    }

    override fun remove(obj: GameObject, mask: Int) {
        val index = index(obj)
        val currentFlags = data.getOrDefault(index, -1)
        data[index] = currentFlags and mask.inv()
    }

    override fun deallocateZone(zoneX: Int, zoneY: Int, level: Int) {
        val zone = Chunk.id(zoneX, zoneY, level)
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                data.remove(index(zone, Tile.index(x, y, ObjectGroup.WALL)))
                data.remove(index(zone, Tile.index(x, y, ObjectGroup.WALL_DECORATION)))
                data.remove(index(zone, Tile.index(x, y, ObjectGroup.INTERACTIVE)))
                data.remove(index(zone, Tile.index(x, y, ObjectGroup.GROUND_DECORATION)))
            }
        }
    }

    override fun clear() {
        data.clear()
    }

    companion object {
        private const val EXPECTED_OBJECT_COUNT = 74_000
        private fun index(obj: GameObject): Int = index(obj.x, obj.y, obj.plane, ObjectGroup.group(obj.type))
        private fun index(x: Int, y: Int, level: Int, group: Int): Int = index(Chunk.tileIndex(x, y, level), Tile.index(x, y, group))
        private fun index(zone: Int, tile: Int): Int = zone or (tile shl 24)
    }
}