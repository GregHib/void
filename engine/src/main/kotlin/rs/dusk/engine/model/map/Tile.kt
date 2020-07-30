package rs.dusk.engine.model.map

import rs.dusk.engine.model.map.area.Coordinate3D
import rs.dusk.engine.model.map.chunk.Chunk
import rs.dusk.engine.model.map.region.Region
import rs.dusk.engine.model.map.region.RegionPlane

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Tile(override val x: Int, override val y: Int, override val plane: Int = 0) :
    Coordinate3D {

    constructor(id: Int) : this(id shr 14 and 0x3fff, id and 0x3fff, id shr 28)

    val id by lazy { getId(x, y, plane) }
    val chunk by lazy { Chunk(x / 8, y / 8, plane) }
    val region by lazy { Region(x / 64, y / 64) }
    val regionPlane by lazy { RegionPlane(x / 64, y / 64, plane) }

    override fun add(x: Int, y: Int, plane: Int) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun equals(x: Int = 0, y: Int = 0, plane: Int = 0) = this.x == x && this.y == y && this.plane == plane
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = minus(x, y, plane)

    fun add(point: Tile) = add(point.x, point.y, point.plane)
    fun minus(point: Tile) = minus(point.x, point.y, point.plane)
    fun delta(point: Tile) = delta(point.x, point.y, point.plane)

    override fun add(x: Int, y: Int) = add(x, y, 0)

    companion object {
        fun createSafe(x: Int, y: Int, plane: Int = 0) =
            Tile(x and 0x3fff, y and 0x3fff, plane and 0x3)

        fun getId(x: Int, y: Int, plane: Int = 0) = (y and 0x3fff) + ((x and 0x3fff) shl 14) + ((plane and 0x3) shl 28)

        val EMPTY = Tile(0)
    }
}