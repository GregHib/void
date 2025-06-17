package world.gregs.voidps.type

import world.gregs.voidps.type.area.Cuboid

@JvmInline
value class RegionLevel(val id: Int) : Coordinate3D<RegionLevel> {

    constructor(x: Int, y: Int, level: Int) : this(id(x, y, level))

    override val x: Int
        get() = x(id)
    override val y: Int
        get() = y(id)
    override val level: Int
        get() = level(id)
    val region: Region
        get() = Region(x, y)
    val zone: Zone
        get() = Zone(x shl 3, y shl 3, level)
    val tile: Tile
        get() = Tile(x shl 6, y shl 6, level)

    override fun copy(x: Int, y: Int, level: Int) = RegionLevel(x, y, level)

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
