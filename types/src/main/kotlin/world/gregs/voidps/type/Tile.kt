package world.gregs.voidps.type

import world.gregs.voidps.type.area.Cuboid

@JvmInline
value class Tile(val id: Int) : Coordinate3D<Tile> {

    constructor(x: Int, y: Int, level: Int = 0) : this(id(x, y, level))

    override val x: Int
        get() = x(id)
    override val y: Int
        get() = y(id)
    override val level: Int
        get() = level(id)

    val zone: Zone
        get() = Zone(x shr 3, y shr 3, level)
    val region: Region
        get() = Region(x shr 6, y shr 6)
    val regionLevel: RegionLevel
        get() = RegionLevel(x shr 6, y shr 6, level)

    override fun copy(x: Int, y: Int, level: Int) = Tile(x, y, level)

    fun distanceTo(other: Tile, width: Int, height: Int) = distanceTo(Distance.getNearest(other, width, height, this))

    fun distanceTo(other: Tile): Int {
        if (level != other.level) {
            return -1
        }
        return Distance.chebyshev(x, y, other.x, other.y)
    }

    fun within(other: Tile, radius: Int): Boolean = Distance.within(x, y, level, other.x, other.y, other.level, radius)

    fun within(x: Int, y: Int, level: Int, radius: Int): Boolean = Distance.within(this.x, this.y, this.level, x, y, level, radius)

    fun toCuboid(width: Int = 1, height: Int = 1) = Cuboid(this, width, height, 1)
    fun toCuboid(radius: Int) = Cuboid(minus(radius, radius), radius * 2 + 1, radius * 2 + 1, 1)

    override fun toString(): String = "Tile($x, $y, $level)"

    companion object {
        fun id(x: Int, y: Int, level: Int = 0) = (y and 0x3fff) + ((x and 0x3fff) shl 14) + ((level and 0x3) shl 28)
        fun x(id: Int) = id shr 14 and 0x3fff
        fun y(id: Int) = id and 0x3fff
        fun level(id: Int) = id shr 28 and 0x3

        val EMPTY = Tile(0)

        fun fromMap(map: Map<String, Any>) = Tile(map["x"] as Int, map["y"] as Int, map["level"] as? Int ?: 0)
        fun fromArray(array: IntArray) = Tile(array[0], array[1], array.getOrNull(2) ?: 0)
        fun fromArray(array: List<Int>) = Tile(array[0], array[1], array.getOrNull(2) ?: 0)

        /**
         * Index for a tile within a [Zone]
         * Used for indexing tiles in arrays
         */
        fun index(x: Int, y: Int): Int = (x and 0x7) or ((y and 0x7) shl 3)
        fun index(x: Int, y: Int, layer: Int): Int = index(x, y) or ((layer and 0x7) shl 6)
        fun indexX(index: Int) = index and 0x7
        fun indexY(index: Int) = index shr 3 and 0x7
        fun indexLayer(index: Int) = index shr 6 and 0x7
    }
}

fun Tile.equals(x: Int = this.x, y: Int = this.y, level: Int = this.level) = this.x == x && this.y == y && this.level == level
