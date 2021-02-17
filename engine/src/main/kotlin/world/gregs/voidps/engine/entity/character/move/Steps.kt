package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Direction


class Steps {
    val steps = IntArray(max)
    var write = 0
    var read = 0

    fun add(direction: Direction) {
        if (write <= 64) {
            steps[write++] = direction.ordinal
        }
    }

    fun addFirst(direction: Direction) {
        steps[--write] = direction.ordinal
    }

    fun peek(): Direction? {
        if (isEmpty()) {
            return null
        }
        return Direction.values[steps[read]]
    }

    fun poll() = Direction.values[steps[read++]]

    fun count() = write - read

    fun isEmpty() = read >= write

    fun clear() {
        write = 0
        read = 0
    }

    fun writeIndex(index: Int) {
        if (index <= 64) {
            write = index
        }
    }

    companion object {
        const val max = 64
    }
}