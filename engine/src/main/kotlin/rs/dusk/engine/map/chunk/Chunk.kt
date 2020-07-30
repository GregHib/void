package rs.dusk.engine.map.chunk

import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.area.Coordinate3D
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.RegionPlane

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 20, 2020
 */
data class Chunk(override val x: Int, override val y: Int, override val plane: Int = 0) :
    Coordinate3D {

    constructor(id: Int) : this(id shr 12, id and 0xfff, id shr 24)

    val id by lazy { getId(x, y, plane) }
    val region by lazy { Region(x / 8, y / 8) }
    val regionPlane by lazy { RegionPlane(x / 8, y / 8, plane) }
    val tile by lazy { Tile(x * 8, y * 8, plane) }

    override fun add(x: Int, y: Int, plane: Int) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, -plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = minus(x, y, plane)
    fun equals(x: Int, y: Int, plane: Int) = this.x == x && this.y == y && this.plane == plane

    fun add(point: Chunk) = add(point.x, point.y, point.plane)
    fun minus(point: Chunk) = minus(point.x, point.y, point.plane)
    fun delta(point: Chunk) = delta(point.x, point.y, point.plane)

    override fun add(x: Int, y: Int) = add(x, y, 0)

    companion object {
        fun createSafe(x: Int, y: Int, plane: Int) =
            Chunk(x and 0xfff, y and 0xfff, plane and 0x3)
        fun getId(x: Int, y: Int, plane: Int) = (y and 0xfff) + ((x and 0xfff) shl 12) + ((plane and 0x3) shl 24)
        val EMPTY = Chunk(0, 0, 0)

    }
}