package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.zone.Zone

@JvmInline
value class RegionLevel(val id: Int) {

    constructor(x: Int, y: Int, level: Int) : this(id(x, y, level))

    val x: Int
        get() = x(id)
    val y: Int
        get() = y(id)
    val level: Int
        get() = level(id)
    val region: Region
        get() = Region(x, y)
    val zone: Zone
        get() = Zone(x shl 3, y shl 3, level)
    val tile: Tile
        get() = Tile(x shl 6, y shl 6, level)

    fun copy(x: Int = this.x, y: Int = this.y, level: Int = this.level) = RegionLevel(x, y, level)
    fun add(x: Int, y: Int, level: Int = 0) = copy(x = this.x + x, y = this.y + y, level = this.level + level)
    fun minus(x: Int = 0, y: Int = 0, level: Int = 0) = add(-x, -y, level)
    fun delta(x: Int = 0, y: Int = 0, level: Int = 0) = Delta(this.x - x, this.y - y, this.level - level)

    fun add(delta: Delta) = add(delta.x, delta.y)
    fun minus(delta: Delta) = minus(delta.x, delta.y)

    fun add(direction: Direction) = add(direction.delta)
    fun minus(direction: Direction) = minus(direction.delta)

    fun add(point: RegionLevel) = add(point.x, point.y, point.level)
    fun minus(point: RegionLevel) = minus(point.x, point.y, point.level)
    fun delta(point: RegionLevel) = delta(point.x, point.y, point.level)

    fun toCuboid(width: Int = 1, height: Int = 1, levels: Int = 1) = Cuboid(tile, width * 64, height * 64, levels)
    fun toCuboid(radius: Int, levels: Int = 1) = Cuboid(minus(radius, radius).tile, (radius * 2 + 1) * 64, (radius * 2 + 1) * 64, levels)

    companion object {
        fun id(x: Int, y: Int, level: Int) = (y and 0xff) + ((x and 0xff) shl 8) + ((level and 0x3) shl 16)
        fun x(id: Int) = id shr 8 and 0xff
        fun y(id: Int) = id and 0xff
        fun level(id: Int) = id shr 16
        val EMPTY = RegionLevel(0, 0, 0)
    }
}
fun RegionLevel.equals(x: Int = 0, y: Int = 0, level: Int = 0) = this.x == x && this.y == y && this.level == level