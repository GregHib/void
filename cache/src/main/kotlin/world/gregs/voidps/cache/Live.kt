package world.gregs.voidps.cache

import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class Live {
    private val indices: MutableSet<Int> = IntOpenHashSet()
    private val configs: MutableSet<Int> = IntOpenHashSet()

    fun addIndex(index: Int) {
        indices.add(index)
    }

    fun addConfig(index: Int) {
        configs.add(index)
    }
}