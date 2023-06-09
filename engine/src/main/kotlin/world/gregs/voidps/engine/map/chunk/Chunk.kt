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
    val index: Int
        get() = index(x, y, plane)

    fun copy(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = Chunk(x, y, plane)
    fun add(x: Int, y: Int, plane: Int = 0) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, -plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = Delta(this.x - x, this.y - y, this.plane - plane)

    fun add(point: Chunk) = add(point.x, point.y, point.plane)
    fun minus(point: Chunk) = minus(point.x, point.y, point.plane)
    fun delta(point: Chunk) = delta(point.x, point.y, point.plane)

    fun add(delta: Delta) = add(delta.x, delta.y, delta.plane)

    fun safeMinus(chunk: Chunk) = safeMinus(chunk.x, chunk.y, chunk.plane)
    fun safeMinus(x: Int = 0, y: Int = 0, plane: Int = 0): Chunk {
        return Chunk((this.x - x).coerceAtLeast(0), (this.y - y).coerceAtLeast(0), (this.plane - plane).coerceAtLeast(0))
    }

    fun toRectangle(radius: Int) = Rectangle(safeMinus(radius, radius).tile, (radius * 2 + 1) * 8, (radius * 2 + 1) * 8)
    fun toRectangle(width: Int = 1, height: Int = 1) = Rectangle(tile, width * 8, height * 8)
    fun toCuboid(width: Int = 1, height: Int = 1) = Cuboid(tile, width * 8, height * 8, 1)
    fun toCuboid(radius: Int) = Cuboid(safeMinus(radius, radius).tile, (radius * 2 + 1) * 8, (radius * 2 + 1) * 8, 1)

    override fun toString(): String {
        return "Chunk($x, $y, $plane)"
    }

    companion object {
        fun id(x: Int, y: Int, plane: Int) = (y and 0xfff) + ((x and 0xfff) shl 12) + ((plane and 0x3) shl 24)
        fun x(id: Int) = id shr 12 and 0xfff
        fun y(id: Int) = id and 0xfff
        fun plane(id: Int) = id shr 24
        val EMPTY = Chunk(0, 0, 0)

        /**
         * Index; used for indexing chunks in arrays
         * @see world.gregs.voidps.engine.map.collision.Collisions
         * @see world.gregs.voidps.engine.entity.obj.GameObjectMap
         */
        fun indexX(index: Int) = index and 0x7ff
        fun indexY(index: Int) = index shr 11 and 0x7ff
        fun indexLevel(index: Int) = index shr 22 and 0x3
        fun index(x: Int, y: Int, level: Int): Int = (x and 0x7ff) or ((y and 0x7ff) shl 11) or ((level and 0x3) shl 22)
        fun indexTile(tileX: Int, tileY: Int, level: Int): Int = index(tileX shr 3, tileY shr 3, level)
    }
}

fun Chunk.equals(x: Int, y: Int, plane: Int) = this.x == x && this.y == y && this.plane == plane