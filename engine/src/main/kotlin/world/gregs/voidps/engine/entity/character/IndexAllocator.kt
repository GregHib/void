package world.gregs.voidps.engine.entity.character

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Gives out a unique number between 1 and [max] for indexing [Character]s
 */
class IndexAllocator(private val max: Int) {
    private val free: Queue<Int> = ConcurrentLinkedQueue()

    init {
        init()
    }

    fun init() {
        for (i in 1 .. max) {
            free.add(i)
        }
    }

    fun release(index: Int) {
        if (index > max) {
            throw IllegalArgumentException("Invalid index $index - Max: $max")
        }
        free.add(index)
    }

    fun obtain(): Int? {
        return free.poll()
    }

    fun clear() {
        free.clear()
        init()
    }
}