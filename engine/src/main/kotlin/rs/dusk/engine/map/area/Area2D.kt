package rs.dusk.engine.map.area

open class Area2D<T : Coordinate2D>(val coords: T, val width: Int, val height: Int) : Iterable<T> {
    fun contains(x: Int, y: Int) = within(x, coords.x, width) && within(y, coords.y, height)

    open fun contains(chunk: T) = contains(chunk.x, chunk.y)

    override fun iterator(): Iterator<T> =
        AreaIterator(coords, width, height)

    @Suppress("UNCHECKED_CAST")
    internal class AreaIterator<T : Coordinate2D>(private val coords: T, width: Int, private val height: Int) : Iterator<T> {

        private val max = width * height
        private var index = 0

        override fun hasNext() = index < max

        override fun next() = coords.add(x = index / height, y = index++.rem(height)) as T

    }

    protected fun within(value: Int, comparator: Int, range: Int) = value >= comparator && value < comparator + range

}