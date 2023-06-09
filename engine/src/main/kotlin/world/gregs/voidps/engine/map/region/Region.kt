package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Id
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.area.Rectangle

@JvmInline
value class Region(override val id: Int) : Id {

    constructor(x: Int, y: Int) : this(id(x, y))

    val x: Int
        get() = x(id)
    val y: Int
        get() = y(id)
    val tile: Tile
        get() = Tile(x shl 6, y shl 6, 0)

    fun copy(x: Int = this.x, y: Int = this.y) = Region(x, y)
    fun add(x: Int, y: Int) = copy(x = this.x + x, y = this.y + y)
    fun minus(x: Int = 0, y: Int = 0) = add(-x, -y)
    fun delta(x: Int = 0, y: Int = 0) = Delta(this.x - x, this.y - y)

    fun add(point: Region) = add(point.x, point.y)
    fun minus(point: Region) = minus(point.x, point.y)
    fun delta(point: Region) = delta(point.x, point.y)

    fun toPlane(plane: Int) = RegionPlane(x, y, plane)

    fun toRectangle(radius: Int) = Rectangle(minus(radius, radius).tile, (radius * 2 + 1) * 64, (radius * 2 + 1) * 64)
    fun toRectangle(width: Int = 1, height: Int = 1) = Rectangle(tile, width * 64, height * 64)
    fun toCuboid(width: Int = 1, height: Int = 1) = Cuboid(tile, width * 64, height * 64, 4)
    fun toCuboid(radius: Int) = Cuboid(minus(radius, radius).tile, (radius * 2 + 1) * 64, (radius * 2 + 1) * 64, 4)

    fun offset(region: Region): Tile {
        return tile.minus(region.tile)
    }

    companion object {
        fun id(x: Int, y: Int) = (y and 0xff) + ((x and 0xff) shl 8)
        fun x(id: Int) = id shr 8
        fun y(id: Int) = id and 0xff
        val EMPTY = Region(0, 0)
    }
}

fun Region.equals(x: Int, y: Int) = this.x == x && this.y == y