package world.gregs.voidps.engine.map.chunk

import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Id
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionPlane

@JvmInline
value class Chunk(override val id: Int) : Id {

    constructor(x: Int, y: Int, plane: Int = 0) : this(getId(x, y, plane))

    val x: Int
        get() = getX(id)
    val y: Int
        get() = getY(id)
    val plane: Int
        get() = getPlane(id)
    val region: Region
        get() = Region(x / 8, y / 8)
    val regionPlane: RegionPlane
        get() = RegionPlane(x / 8, y / 8, plane)
    val tile: Tile
        get() = Tile(x * 8, y * 8, plane)

    fun copy(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = Chunk(x, y, plane)
    fun add(x: Int, y: Int, plane: Int = 0) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, -plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = Delta(this.x - x, this.y - y, this.plane - plane)

    fun add(point: Chunk) = add(point.x, point.y, point.plane)
    fun minus(point: Chunk) = minus(point.x, point.y, point.plane)
    fun delta(point: Chunk) = delta(point.x, point.y, point.plane)

    fun add(delta: Delta) = add(delta.x, delta.y, delta.plane)

    fun toCuboid(width: Int = 1, height: Int = 1) = Cuboid(tile, width * 8, height * 8, 1)
    fun toCuboid(radius: Int) = Cuboid(minus(radius, radius).tile, (radius * 2 + 1) * 8, (radius * 2 + 1) * 8, 1)
    fun toRectangle(radius: Int) = Rectangle(minus(radius, radius).tile, (radius * 2 + 1) * 8, (radius * 2 + 1) * 8)
    fun toRectangle(width: Int = 1, height: Int = 1) = Rectangle(tile, width * 8, height * 8)

    companion object {
        fun createSafe(x: Int, y: Int, plane: Int) = Chunk(x and 0xfff, y and 0xfff, plane and 0x3)
        fun getId(x: Int, y: Int, plane: Int) = (y and 0xfff) + ((x and 0xfff) shl 12) + ((plane and 0x3) shl 24)
        fun getX(id: Int) = id shr 12 and 0xfff
        fun getY(id: Int) = id and 0xfff
        fun getPlane(id: Int) = id shr 24
        val EMPTY = Chunk(0, 0, 0)
    }
}

fun Chunk.equals(x: Int, y: Int, plane: Int) = this.x == x && this.y == y && this.plane == plane