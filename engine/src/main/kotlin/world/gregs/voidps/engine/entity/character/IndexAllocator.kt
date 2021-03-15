package world.gregs.voidps.engine.entity.character

import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
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
}