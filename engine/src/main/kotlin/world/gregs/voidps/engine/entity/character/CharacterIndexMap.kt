package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap

/**
 * High-performance spatial index for grouping character indices by [world.gregs.voidps.type.Zone] or [world.gregs.voidps.type.Region]
 * i.e. Map<Zone, List<Index>>
 * It's a doubly linked list with head [table] for fast iteration.
 * ```
 *  listOf(player1, player3, player2)
 *  players  [player1] [player2] [player3]
 *  next     [player3] [-1]      [player2]
 *  previous [-1]      [player3] [player1]
 *  ```
 */
class CharacterIndexMap(size: Int) {
    private val table = Int2IntOpenHashMap(size)
    init {
        table.defaultReturnValue(INVALID)
    }
    val next = IntArray(size) { INVALID }
    val previous = IntArray(size) { INVALID }

    fun add(id: Int, index: Int) {
        val head = table.get(id)
        if (head == index) {
            return
        }
        previous[index] = INVALID
        next[index] = head
        if (head != INVALID) {
            previous[head] = index
        }
        table.put(id, index)
    }

    fun remove(id: Int, index: Int) {
        val p = previous[index]
        val n = next[index]
        if (p != INVALID) {
            next[p] = n
        } else if (n != INVALID) {
            table.put(id, n)
        }
        if (n != INVALID) {
            previous[n] = p
        }
        previous[index] = INVALID
        next[index] = INVALID
    }

    fun clear() {
        table.clear()
        next.fill(INVALID)
        previous.fill(INVALID)
    }

    fun onEach(id: Int, action: (Int) -> Unit) {
        var index = table.get(id)
        while (index != INVALID) {
            action(index)
            index = next[index]
        }
    }

    companion object {
        private const val INVALID = -1
    }
}