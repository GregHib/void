package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Id
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.zone.Zone

@JvmInline
value class RegionPlane(override val id: Int) : Id {

    constructor(x: Int, y: Int, plane: Int) : this(id(x, y, plane))

    val x: Int
        get() = x(id)
    val y: Int
        get() = y(id)
    val plane: Int
        get() = plane(id)
    val region: Region
        get() = Region(x, y)
    val zone: Zone
        get() = Zone(x shl 3, y shl 3, plane)
    val tile: Tile
        get() = Tile(x shl 6, y shl 6, plane)

    fun copy(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = RegionPlane(x, y, plane)
    fun add(x: Int, y: Int, plane: Int = 0) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = Delta(this.x - x, this.y - y, this.plane - plane)

    fun add(delta: Delta) = add(delta.x, delta.y)
    fun minus(delta: Delta) = minus(delta.x, delta.y)

    fun add(direction: Direction) = add(direction.delta)
    fun minus(direction: Direction) = minus(direction.delta)

    fun add(point: RegionPlane) = add(point.x, point.y, point.plane)
    fun minus(point: RegionPlane) = minus(point.x, point.y, point.plane)
    fun delta(point: RegionPlane) = delta(point.x, point.y, point.plane)

    fun toCuboid(width: Int = 1, height: Int = 1, planes: Int = 1) = Cuboid(tile, width * 64, height * 64, planes)
    fun toCuboid(radius: Int, planes: Int = 1) = Cuboid(minus(radius, radius).tile, (radius * 2 + 1) * 64, (radius * 2 + 1) * 64, planes)

    companion object {
        fun id(x: Int, y: Int, plane: Int) = (y and 0xff) + ((x and 0xff) shl 8) + ((plane and 0x3) shl 16)
        fun x(id: Int) = id shr 8 and 0xff
        fun y(id: Int) = id and 0xff
        fun plane(id: Int) = id shr 16
        val EMPTY = RegionPlane(0, 0, 0)
    }
}
fun RegionPlane.equals(x: Int = 0, y: Int = 0, plane: Int = 0) = this.x == x && this.y == y && this.plane == plane