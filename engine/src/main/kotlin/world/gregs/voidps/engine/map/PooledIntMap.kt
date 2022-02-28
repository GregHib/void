package world.gregs.voidps.engine.map

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.io.pool.ObjectPool

open class PooledIntMap<Coll : MutableCollection<Value>, Value : Any>(
    private val pool: ObjectPool<Coll>
) : Iterable<MutableMap.MutableEntry<Int, Coll>> {
    val map = Int2ObjectOpenHashMap<Coll>()

    operator fun get(key: Int): Coll? = map.get(key)

    fun containsKey(key: Int) = map.containsKey(key)

    fun clear() {
        for (set in map.values) {
            pool.recycle(set)
        }
        map.clear()
    }

    fun add(key: Int, value: Value): Boolean {
        var list = map[key]
        if (list == null) {
            list = pool.borrow()
            map[key] = list
        }
        return list.add(value)
    }

    fun remove(key: Int, value: Value): Boolean {
        val set = map[key] ?: return false
        val removed = set.remove(value)
        if (removed && set.isEmpty()) {
            map.remove(key)
            pool.recycle(set)
        }
        return removed
    }

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Coll>> {
        return map.iterator()
    }

}