package world.gregs.voidps.engine.map.zone

import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Id
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionPlane

/**
 * Represents a 8x8 tiled area
 */
@JvmInline
value class Zone(override val id: Int) : Id {

    constructor(x: Int, y: Int, plane: Int = 0) : this(id(x, y, plane))

    val x: Int
        get() = x(id)
    val y: Int
        get() = y(id)
    val plane: Int
        get() = plane(id)
    val region: Region
        get() = Region(x shr 3, y shr 3)
    val regionPlane: RegionPlane
        get() = RegionPlane(x shr 3, y shr 3, plane)
    val tile: Tile
        get() = Tile(x shl 3, y shl 3, plane)

    fun copy(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = Zone(x, y, plane)
    fun add(x: Int, y: Int, plane: Int = 0) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, -plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = Delta(this.x - x, this.y - y, this.plane - plane)

    fun add(point: Zone) = add(point.x, point.y, point.plane)
    fun minus(point: Zone) = minus(point.x, point.y, point.plane)
    fun delta(point: Zone) = delta(point.x, point.y, point.plane)

    fun add(delta: Delta) = add(delta.x, delta.y, delta.plane)

    fun safeMinus(zone: Zone) = safeMinus(zone.x, zone.y, zone.plane)
    fun safeMinus(x: Int = 0, y: Int = 0, plane: Int = 0): Zone {
        return Zone((this.x - x).coerceAtLeast(0), (this.y - y).coerceAtLeast(0), (this.plane - plane).coerceAtLeast(0))
    }

    fun toRectangle(radius: Int) = Rectangle(safeMinus(radius, radius).tile, (radius * 2 + 1) * 8, (radius * 2 + 1) * 8)
    fun toRectangle(width: Int = 1, height: Int = 1) = Rectangle(tile, width * 8, height * 8)
    fun toCuboid(width: Int = 1, height: Int = 1) = Cuboid(tile, width * 8, height * 8, 1)
    fun toCuboid(radius: Int) = Cuboid(safeMinus(radius, radius).tile, (radius * 2 + 1) * 8, (radius * 2 + 1) * 8, 1)

    override fun toString(): String {
        return "Zone($x, $y, $plane)"
    }

    companion object {
        fun id(x: Int, y: Int, plane: Int) = (x and 0x7ff) + ((y and 0x7ff) shl 11) + ((plane and 0x3) shl 22)
        fun x(id: Int) = id and 0x7ff
        fun y(id: Int) = id shr 11 and 0x7ff
        fun plane(id: Int) = id shr 22 and 0x3
        val EMPTY = Zone(0, 0, 0)

        /**
         * Index of a local tile within a zone
         */
        fun tileIndex(tileX: Int, tileY: Int, level: Int): Int = id(tileX shr 3, tileY shr 3, level)
    }
}

fun Zone.equals(x: Int, y: Int, plane: Int) = this.x == x && this.y == y && this.plane == plane