package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap

/**
 * Spatial index for grouping character indices by [world.gregs.voidps.type.Zone] or [world.gregs.voidps.type.Region]
 * i.e. Map<Zone, List<Index>>
 * Each group is a doubly linked list through [next] and [previous] so moving between groups doesn't allocate.
 */
class CharacterIndexMap(size: Int) {
    /**
     * First index in each group
     */
    private val heads = Int2IntOpenHashMap(size).apply { defaultReturnValue(INVALID) }
    private val next = IntArray(size) { INVALID }
    private val previous = IntArray(size) { INVALID }

    /**
     * Which group the index is currently in
     * Used for moving a character between groups and ignoring repeated adds and removes
     */
    private val current = IntArray(size) { INVALID }

    /**
     * Insert [index] into the group [id]
     * Removes from the current group if already present
     */
    fun add(id: Int, index: Int) {
        if (index < 0) {
            return
        }
        val existing = current[index]
        if (existing == id) {
            return
        }
        if (existing != INVALID) {
            unlink(existing, index)
        }
        val head = heads.get(id)
        next[index] = head
        previous[index] = INVALID
        if (head != INVALID) {
            previous[head] = index
        }
        heads.put(id, index)
        current[index] = id
    }

    /**
     * Removes [index] from group [id]
     */
    fun remove(id: Int, index: Int) {
        if (index < 0 || current[index] != id) {
            return
        }
        unlink(id, index)
    }

    private fun unlink(id: Int, index: Int) {
        val before = previous[index]
        val after = next[index]
        if (before != INVALID) {
            next[before] = after
        } else if (after != INVALID) {
            heads.put(id, after)
        } else {
            heads.remove(id)
        }
        if (after != INVALID) {
            previous[after] = before
        }
        next[index] = INVALID
        previous[index] = INVALID
        current[index] = INVALID
    }

    fun clear() {
        heads.clear()
        next.fill(INVALID)
        previous.fill(INVALID)
        current.fill(INVALID)
    }

    fun onEach(id: Int, action: (Int) -> Unit) {
        var index = heads.get(id)
        while (index != INVALID) {
            val following = next[index]
            action(index)
            index = following
        }
    }

    companion object {
        private const val INVALID = -1
    }
}
