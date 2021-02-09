package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Coordinate3D
import world.gregs.voidps.engine.map.chunk.Chunk

/**
 * @author GregHib <greg@gregs.world>
 * @since April 16, 2020
 */
inline class RegionPlane(val id: Int) : Coordinate3D {

    constructor(x: Int, y: Int, plane: Int) : this(getId(x, y, plane))

    override val x: Int
        get() = getX(id)
    override val y: Int
        get() = getY(id)
    override val plane: Int
        get() = getPlane(id)
    val region: Region
        get() = Region(x, y)
    val chunk: Chunk
        get() = Chunk(x * 8, y * 8, plane)
    val tile: Tile
        get() = Tile(x * 64, y * 64, plane)

    fun copy(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = RegionPlane(x, y, plane)
    override fun add(x: Int, y: Int, plane: Int) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
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
        fun getX(id: Int) = id shr 8 and 0xff
        fun getY(id: Int) = id and 0xff
        fun getPlane(id: Int) = id shr 16
        val EMPTY = RegionPlane(0, 0, 0)
    }
}
fun RegionPlane.equals(x: Int = 0, y: Int = 0, plane: Int = 0) = this.x == x && this.y == y && this.plane == plane