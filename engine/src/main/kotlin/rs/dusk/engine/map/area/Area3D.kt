package rs.dusk.engine.map.area

class Area3D<T : Coordinate3D>(coords: T, width: Int, height: Int, val planes: Int) : Area2D<T>(coords, width, height) {

    fun contains(x: Int, y: Int, plane: Int) = super.contains(x, y) && within(plane, coords.plane, planes)

    override fun contains(chunk: T) = contains(chunk.x, chunk.y, chunk.plane)

    override fun iterator(): Iterator<T> =
        AreaIterator(coords, width, height, planes)

    @Suppress("UNCHECKED_CAST")
    internal class AreaIterator<T : Coordinate3D>(private val coords: T, private val width: Int, private val height: Int, planes: Int) :
        Iterator<T> {

        private val max = width * height * planes
        private var index = 0

        override fun hasNext() = index < max

        override fun next(): T {
            val coords = coords.add(
                x = index.rem(width * height) / height,
                y = index.rem(width * height).rem(height),
                plane = index / (width * height)
            ) as T
            index++
            return coords
        }

    }
}