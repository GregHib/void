package world.gregs.voidps.engine.entity.character

import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Gives out a unique number between 1 and [max] for indexing [Character]s
 */
class IndexAllocator(private val max: Int) {
    var cap = 1
    val free: Deque<Int> = ConcurrentLinkedDeque()

    fun release(index: Int) {
        if (index > cap) {
            throw IllegalArgumentException("Invalid index $index - Cap: $cap Max: $max")
        }
        free.push(index)
    }

    fun obtain(): Int? {
        if (free.isEmpty()) {
            // increase cap
            if (cap < max) {
                free.push(cap++)
            } else {
                return null
            }
        }
        return free.poll()
    }

    fun clear() {
        free.clear()
        cap = 1
    }
}