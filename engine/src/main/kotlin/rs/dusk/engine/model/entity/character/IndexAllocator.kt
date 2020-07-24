package rs.dusk.engine.model.entity.character

import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
class IndexAllocator(private val max: Int) {
    var cap = 1
    val free: Deque<Int> = LinkedList()

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