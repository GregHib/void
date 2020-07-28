package rs.dusk.engine.model.world

data class Area<T : Coordinates>(val coords: T, val width: Int, val height: Int) : Iterable<T> {
    fun contains(x: Int, y: Int) = x >= coords.x && x <= coords.x + width && y >= coords.y && y <= coords.y + height

    fun contains(chunk: T) = contains(chunk.x, chunk.y)

    override fun iterator(): Iterator<T> =
        AreaIterator(coords, width, height)

    internal class AreaIterator<T : Coordinates>(private val coords: T, private val width: Int, height: Int) : Iterator<T> {

        private val max = width * height
        private var index = 0

        override fun hasNext() = index < max

        override fun next() = coords.add(x = index / width, y = index++.rem(width)) as T

    }
}