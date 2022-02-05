package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Id
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.chunk.Chunk

@JvmInline
value class RegionPlane(override val id: Int) : Id {

    constructor(x: Int, y: Int, plane: Int) : this(getId(x, y, plane))

    val x: Int
        get() = getX(id)
    val y: Int
        get() = getY(id)
    val plane: Int
        get() = getPlane(id)
    val region: Region
        get() = Region(x, y)
    val chunk: Chunk
        get() = Chunk(x * 8, y * 8, plane)
    val tile: Tile
        get() = Tile(x * 64, y * 64, plane)

    fun copy(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = RegionPlane(x, y, plane)
    fun add(x: Int, y: Int, plane: Int = 0) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = Delta(this.x - x, this.y - y, this.plane - plane)

    fun add(point: RegionPlane) = add(point.x, point.y, point.plane)
    fun minus(point: RegionPlane) = minus(point.x, point.y, point.plane)
    fun delta(point: RegionPlane) = delta(point.x, point.y, point.plane)

    fun toCuboid(width: Int = 1, height: Int = 1, planes: Int = 1) = Cuboid(tile, width * 64, height * 64, planes)

    fun toCuboid(radius: Int, planes: Int = 1) = Cuboid(minus(radius, radius).tile, (radius * 2 + 1) * 64, (radius * 2 + 1) * 64, planes)

    companion object {
        fun createSafe(x: Int, y: Int, plane: Int) = RegionPlane(x and 0xff, y and 0xff, plane and 0x3)
        fun getId(x: Int, y: Int, plane: Int) = (y and 0xff) + ((x and 0xff) shl 8) + ((plane and 0x3) shl 16)
        fun getX(id: Int) = id shr 8 and 0xff
        fun getY(id: Int) = id and 0xff
        fun getPlane(id: Int) = id shr 16
        val EMPTY = RegionPlane(0, 0, 0)
    }
}
fun RegionPlane.equals(x: Int = 0, y: Int = 0, plane: Int = 0) = this.x == x && this.y == y && this.plane == plane