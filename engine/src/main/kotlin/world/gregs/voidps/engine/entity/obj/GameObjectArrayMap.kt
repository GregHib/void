package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Much like [Collisions] this stores [GameObject]s by zone + group
 * Large memory footprint but faster then [GameObjectHashMap] when storing millions of objects.
 */
class GameObjectArrayMap : GameObjectMap {
    private val data: Array<IntArray?> = arrayOfNulls(TOTAL_ZONE_COUNT)

    override fun get(obj: GameObject) = get(obj.x, obj.y, obj.plane, ObjectGroup.group(obj.shape))

    override operator fun get(x: Int, y: Int, level: Int, group: Int): Int {
        val zoneIndex = Chunk.tileIndex(x, y, level)
        val tileIndex = Tile.index(x, y, group)
        return data[zoneIndex]?.get(tileIndex) ?: -1
    }

    override operator fun set(zone: Int, tile: Int, mask: Int) {
        val tiles = data[zone] ?: allocateIfAbsent(zone)
        tiles[tile] = mask
    }

    override operator fun set(x: Int, y: Int, level: Int, group: Int, mask: Int) {
        val tiles = data[Chunk.tileIndex(x, y, level)] ?: allocateIfAbsent(x, y, level)
        tiles[Tile.index(x, y, group)] = mask
    }

    override fun add(obj: GameObject, mask: Int) {
        val x = obj.x
        val y = obj.y
        val level = obj.plane
        val group = ObjectGroup.group(obj.shape)
        val zoneIndex = Chunk.tileIndex(x, y, level)
        val tileIndex = Tile.index(x, y, group)
        val currentFlags = data[zoneIndex]?.get(tileIndex) ?: 0
        this[x, y, level, group] = currentFlags or mask
    }

    override fun remove(obj: GameObject, mask: Int) {
        val x = obj.x
        val y = obj.y
        val level = obj.plane
        val group = ObjectGroup.group(obj.shape)
        val currentFlags = this[x, y, level, group]
        this[x, y, level, group] = currentFlags and mask.inv()
    }

    private fun allocateIfAbsent(absoluteX: Int, absoluteY: Int, level: Int): IntArray {
        val zoneIndex = Chunk.tileIndex(absoluteX, absoluteY, level)
        return allocateIfAbsent(zoneIndex)
    }

    private fun allocateIfAbsent(zoneIndex: Int): IntArray {
        val existingFlags = data[zoneIndex]
        if (existingFlags != null) return existingFlags
        val tileFlags = IntArray(ZONE_TILE_COUNT)
        data[zoneIndex] = tileFlags
        return tileFlags
    }

    override fun deallocateZone(zoneX: Int, zoneY: Int, level: Int) {
        data[Chunk.tileIndex(zoneX, zoneY, level)] = null
    }

    fun isZoneAllocated(absoluteX: Int, absoluteY: Int, level: Int): Boolean {
        return data[Chunk.tileIndex(absoluteX, absoluteY, level)] != null
    }

    override fun clear() {
        data.fill(null)
    }

    companion object {
        private const val TOTAL_ZONE_COUNT: Int = 2048 * 2048 * 4
        private const val ZONE_TILE_COUNT: Int = 8 * 8 * 4
    }
}