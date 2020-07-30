package rs.dusk.engine.map.region

import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.area.Coordinate3D
import rs.dusk.engine.map.chunk.Chunk

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class RegionPlane(override val x: Int, override val y: Int, override val plane: Int) :
    Coordinate3D {

    constructor(id: Int) : this(id shr 8, id and 0xff, id shr 16)

    val id by lazy { getId(x, y, plane) }
    val region by lazy { Region(x, y) }
    val chunk by lazy { Chunk(x * 8, y * 8, plane) }
    val tile by lazy { Tile(x * 64, y * 64, plane) }

    override fun add(x: Int, y: Int, plane: Int) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun equals(x: Int = 0, y: Int = 0, plane: Int = 0) = this.x == x && this.y == y && this.plane == plane
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = minus(x, y, plane)

    fun add(point: RegionPlane) = add(point.x, point.y, point.plane)
    fun minus(point: RegionPlane) = minus(point.x, point.y, point.plane)
    fun delta(point: RegionPlane) = delta(point.x, point.y, point.plane)

    override fun add(x: Int, y: Int) = add(x, y, 0)

    companion object {
        fun createSafe(x: Int, y: Int, plane: Int) =
            RegionPlane(x and 0xff, y and 0xff, plane and 0x3)
        fun getId(x: Int, y: Int, plane: Int) = (y and 0xff) + ((x and 0xff) shl 8) + ((plane and 0x3) shl 16)
        val EMPTY = RegionPlane(0, 0, 0)
    }
}