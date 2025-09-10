package world.gregs.voidps.type

import world.gregs.voidps.type.area.Cuboid

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

    fun toCuboid(width: Int = 1, height: Int = 1, levels: Int = 1) = Cuboid(tile, width * 64, height * 64, levels)
    fun toCuboid(radius: Int, levels: Int = 1) = Cuboid(minus(radius, radius).tile, (radius * 2 + 1) * 64, (radius * 2 + 1) * 64, levels)

    fun add(x: Int = 0, y: Int = 0, level: Int = 0) = copy(this.x + x, this.y + y, this.level + level)
    fun minus(x: Int = 0, y: Int = 0, level: Int = 0) = add(-x, -y, -level)
    fun delta(x: Int = 0, y: Int = 0, level: Int = 0) = Delta(this.x - x, this.y - y, this.level - level)

    fun add(value: RegionLevel) = add(value.x, value.y, value.level)
    fun minus(value: RegionLevel) = minus(value.x, value.y, value.level)
    fun delta(value: RegionLevel) = delta(value.x, value.y, value.level)

    fun add(value: Delta) = add(value.x, value.y, value.level)
    fun minus(value: Delta) = minus(value.x, value.y, value.level)
    fun delta(value: Delta) = delta(value.x, value.y, value.level)

    fun add(direction: Direction) = add(direction.delta)
    fun minus(direction: Direction) = minus(direction.delta)
    fun delta(direction: Direction) = minus(direction.delta)

    fun addX(value: Int) = add(value, 0, 0)
    fun addY(value: Int) = add(0, value, 0)
    fun addLevel(value: Int) = add(0, 0, value)

    companion object {
        fun id(x: Int, y: Int, level: Int) = (y and 0xff) + ((x and 0xff) shl 8) + ((level and 0x3) shl 16)
        fun x(id: Int) = id shr 8 and 0xff
        fun y(id: Int) = id and 0xff
        fun level(id: Int) = id shr 16
        val EMPTY = RegionLevel(0, 0, 0)
    }
}
fun RegionLevel.equals(x: Int = 0, y: Int = 0, level: Int = 0) = this.x == x && this.y == y && this.level == level
