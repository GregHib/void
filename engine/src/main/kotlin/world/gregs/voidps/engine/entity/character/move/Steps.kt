package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Direction
import java.util.*


class Steps {
    val steps = LinkedList<Direction>()

    fun add(direction: Direction) {
        steps.add(direction)
    }

    fun addFirst(direction: Direction) {
        steps.addFirst(direction)
    }

    fun peek(): Direction? {
        if (isEmpty()) {
            return null
        }
        return steps.peek()
    }

    fun poll(): Direction = steps.poll()

    fun count() = steps.count()

    fun isEmpty() = steps.isEmpty()

    fun clear() {
        steps.clear()
    }
}