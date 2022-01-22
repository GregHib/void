package world.gregs.voidps.engine.map

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool

class TileMap<T : Any>(
    capacity: Int,
    private val pool: ObjectPool<ObjectLinkedOpenHashSet<T>> = object : DefaultPool<ObjectLinkedOpenHashSet<T>>(capacity) {
        override fun produceInstance(): ObjectLinkedOpenHashSet<T> = ObjectLinkedOpenHashSet()
    }
) {
    private val tiles = Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<T>>()

    fun containsKey(key: Tile): Boolean {
        return tiles.containsKey(key.id)
    }

    fun get(key: Tile): Set<T> {
        return tiles.get(key.id) ?: emptySet()
    }

    fun isEmpty(): Boolean {
        return tiles.isEmpty()
    }

    fun clear() {
        for (set in tiles.values) {
            set.clear()
            pool.recycle(set)
        }
        tiles.clear()
    }

    operator fun set(key: Tile, value: T): Boolean {
        return tiles.getOrPut(key.id) { pool.borrow() }.add(value)
    }

    fun remove(key: Tile, value: T): Boolean {
        val set = tiles[key.id] ?: return false
        val removed = set.remove(value)
        set.trim()
        if (set.isEmpty()) {
            tiles.remove(key.id)
            pool.recycle(set)
        }
        return removed
    }

}