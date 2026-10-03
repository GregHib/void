package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

/**
 * Spatial index for grouping character indices by [world.gregs.voidps.type.Zone] or [world.gregs.voidps.type.Region]
 * i.e. Map<Zone, List<Index>>
 */
class CharacterIndexMap(size: Int) {
    /**
     * Table mapping tiles to sets
     */
    private val table = Int2ObjectOpenHashMap<IntOpenHashSet>(size)

    /**
     * Which tile set the index is currently in
     * Used for moving a character between tiles
     */
    private val current = IntArray(size) { INVALID }

    /**
     * Insert [index] into the set [id]
     * Removes from the current set if already present
     */
    fun add(id: Int, index: Int) {
        if (index < 0) {
            return
        }
        val existing = current[index]
        if (existing != INVALID) {
            remove(existing, index)
        }
        var set = table.get(id)
        if (set == null) {
            set = IntOpenHashSet()
            table.put(id, set)
        }
        set.add(index)
        current[index] = id
    }

    /**
     * Removes [index] from set [id]
     */
    fun remove(id: Int, index: Int) {
        val set = table.get(id) ?: return
        if (set.remove(index) && set.isEmpty()) {
            table.remove(id)
        }
        current[index] = INVALID
    }

    fun clear() {
        table.clear()
        current.fill(INVALID)
    }

    fun onEach(id: Int, action: (Int) -> Unit) {
        val set = table.get(id) ?: return
        val iterator = set.iterator()
        while (iterator.hasNext()) {
            action(iterator.nextInt())
        }
    }

    companion object {
        private const val INVALID = -1
    }
}
