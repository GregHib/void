package world.gregs.voidps.type

import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Rectangle

/**
 * Represents a 8x8 tiled area
 */
@JvmInline
value class Zone(val id: Int) {

    constructor(x: Int, y: Int, level: Int = 0) : this(id(x, y, level))

    val x: Int
        get() = x(id)
    val y: Int
        get() = y(id)
    val level: Int
        get() = level(id)
    val region: Region
        get() = Region(x shr 3, y shr 3)
    val regionLevel: RegionLevel
        get() = RegionLevel(x shr 3, y shr 3, level)
    val tile: Tile
        get() = Tile(x shl 3, y shl 3, level)

    fun copy(x: Int = this.x, y: Int = this.y, level: Int = this.level) = Zone(x, y, level)

    fun safeMinus(zone: Zone) = safeMinus(zone.x, zone.y, zone.level)
    fun safeMinus(x: Int = 0, y: Int = 0, level: Int = 0): Zone = Zone((this.x - x).coerceAtLeast(0), (this.y - y).coerceAtLeast(0), (this.level - level).coerceAtLeast(0))

    fun toRectangle(radius: Int) = Rectangle(safeMinus(radius, radius).tile, (radius * 2 + 1) * 8, (radius * 2 + 1) * 8)
    fun toRectangle(width: Int = 1, height: Int = 1) = Rectangle(tile, width * 8, height * 8)
    fun toCuboid(width: Int = 1, height: Int = 1) = Cuboid(tile, width * 8, height * 8, 1)
    fun toCuboid(radius: Int) = Cuboid(safeMinus(radius, radius).tile, (radius * 2 + 1) * 8, (radius * 2 + 1) * 8, 1)

    override fun toString(): String = "Zone($x, $y, $level)"

    fun add(x: Int = 0, y: Int = 0, level: Int = 0) = copy(this.x + x, this.y + y, this.level + level)
    fun minus(x: Int = 0, y: Int = 0, level: Int = 0) = add(-x, -y, -level)
    fun delta(x: Int = 0, y: Int = 0, level: Int = 0) = Delta(this.x - x, this.y - y, this.level - level)

    fun add(value: Zone) = add(value.x, value.y, value.level)
    fun minus(value: Zone) = minus(value.x, value.y, value.level)
    fun delta(value: Zone) = delta(value.x, value.y, value.level)

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
        fun id(x: Int, y: Int, level: Int) = (x and 0x7ff) + ((y and 0x7ff) shl 11) + ((level and 0x3) shl 22)
        fun x(id: Int) = id and 0x7ff
        fun y(id: Int) = id shr 11 and 0x7ff
        fun level(id: Int) = id shr 22 and 0x3
        val EMPTY = Zone(0, 0, 0)

        /**
         * Index of a local tile within a zone
         */
        fun tileIndex(tileX: Int, tileY: Int, level: Int): Int = id(tileX shr 3, tileY shr 3, level)
    }
}

fun Zone.equals(x: Int, y: Int, level: Int) = this.x == x && this.y == y && this.level == level
