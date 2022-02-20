package world.gregs.voidps.engine.map

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue
import it.unimi.dsi.fastutil.ints.IntArrayList
import world.gregs.voidps.engine.entity.character.Character

class TileMap<Value : Character>(capacity: Int) {

    val free = IntArrayFIFOQueue(capacity)
    init {
        for (i in 0 until capacity) {
            free.enqueue(i)
        }
    }
    val map = Int2IntOpenHashMap(capacity)
    val data = arrayOfNulls<IntArrayList?>(capacity)

    operator fun get(key: Int): IntArrayList? {
        val index = map.get(key)
        if (index == -1) {
            return null
        }
        return data[index]
    }

    fun containsKey(key: Int) = map.containsKey(key)

    fun clear() {
        free.clear()
        for (i in data.indices) {
            free.enqueue(i)
        }
        map.clear()
    }

    fun add(tile: Tile, value: Value): Boolean {
        return add(tile.id, value.index)
    }
    private fun add(key: Int, value: Int): Boolean {
        var index = map.get(key)
        if (index == -1) {
            index = free.dequeueInt()
            map[key] = index
        }
        var list = data[index]
        if (list == null) {
            list = IntArrayList(16)
            data[index] = list
        }
        return list.add(value)
    }

    fun remove(key: Tile, value: Value): Boolean {
        return remove(key.id, value.index)
    }

    private fun remove(key: Int, value: Int): Boolean {
        val index = map.get(key)
        if (index == -1) {
            return false
        }
        val list = data[index] ?: return false
        val removed = list.remove(value)
        if (list.isEmpty) {
            map.remove(index)
            free.enqueue(index)
        }
        return removed
    }

}