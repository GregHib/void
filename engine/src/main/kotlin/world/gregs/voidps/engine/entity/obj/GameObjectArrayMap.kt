package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Much like [Collisions] this stores [GameObject]s by zone + [ObjectLayer]
 * Large memory footprint but faster then [GameObjectHashMap] when storing millions of objects.
 */
class GameObjectArrayMap : GameObjectMap {
    private val data: Array<IntArray?> = arrayOfNulls(TOTAL_ZONE_COUNT)

    override fun get(obj: GameObject) = get(obj.x, obj.y, obj.level, ObjectLayer.layer(obj.shape))

    override operator fun get(x: Int, y: Int, level: Int, layer: Int): Int {
        val zoneIndex = Zone.tileIndex(x, y, level)
        val tileIndex = Tile.index(x, y, layer)
        return data[zoneIndex]?.get(tileIndex) ?: -1
    }

    override operator fun set(x: Int, y: Int, level: Int, layer: Int, mask: Int) {
        val tiles = allocateIfAbsent(x, y, level)
        tiles[Tile.index(x, y, layer)] = mask
    }

    override fun add(obj: GameObject, mask: Int) {
        val x = obj.x
        val y = obj.y
        val level = obj.level
        val layer = ObjectLayer.layer(obj.shape)
        val zoneIndex = Zone.tileIndex(x, y, level)
        val tileIndex = Tile.index(x, y, layer)
        val currentFlags = data[zoneIndex]?.get(tileIndex) ?: 0
        this[x, y, level, layer] = currentFlags or mask
    }

    override fun remove(obj: GameObject, mask: Int) {
        val x = obj.x
        val y = obj.y
        val level = obj.level
        val layer = ObjectLayer.layer(obj.shape)
        val currentFlags = this[x, y, level, layer]
        this[x, y, level, layer] = currentFlags and mask.inv()
    }

    private fun allocateIfAbsent(absoluteX: Int, absoluteY: Int, level: Int): IntArray {
        val zoneIndex = Zone.tileIndex(absoluteX, absoluteY, level)
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
        data[Zone.tileIndex(zoneX, zoneY, level)] = null
    }

    fun isZoneAllocated(absoluteX: Int, absoluteY: Int, level: Int): Boolean {
        return data[Zone.tileIndex(absoluteX, absoluteY, level)] != null
    }

    override fun clear() {
        data.fill(null)
    }

    companion object {
        private const val TOTAL_ZONE_COUNT: Int = 2048 * 2048 * 4
        private const val ZONE_TILE_COUNT: Int = 8 * 8 * 4
    }
}