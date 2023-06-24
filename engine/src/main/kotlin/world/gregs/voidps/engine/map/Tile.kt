package world.gregs.voidps.engine.map

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionPlane

@JvmInline
value class Tile(override val id: Int) : Id {

    constructor(x: Int, y: Int, plane: Int = 0) : this(id(x, y, plane))

    val x: Int
        get() = x(id)
    val y: Int
        get() = y(id)
    val plane: Int
        get() = plane(id)

    val chunk: Chunk
        get() = Chunk(x shr 3, y shr 3, plane)
    val region: Region
        get() = Region(x shr 6, y shr 6)
    val regionPlane: RegionPlane
        get() = RegionPlane(x shr 6, y shr 6, plane)

    fun copy(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = Tile(x, y, plane)
    fun add(x: Int, y: Int, plane: Int = 0) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)

    fun addX(value: Int) = add(value, 0, 0)
    fun addY(value: Int) = add(0, value, 0)
    fun addPlane(value: Int) = add(0, 0, value)

    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, -plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = Delta(this.x - x, this.y - y, this.plane - plane)

    fun add(point: Tile) = add(point.x, point.y, point.plane)
    fun minus(point: Tile) = minus(point.x, point.y, point.plane)
    fun delta(point: Tile) = delta(point.x, point.y, point.plane)

    fun add(delta: Delta) = add(delta.x, delta.y, delta.plane)
    fun minus(delta: Delta) = minus(delta.x, delta.y, delta.plane)
    fun delta(delta: Delta) = delta(delta.x, delta.y, delta.plane)

    fun add(direction: Direction) = add(direction.delta)
    fun minus(direction: Direction) = minus(direction.delta)
    fun delta(direction: Direction) = delta(direction.delta)

    fun distanceTo(entity: Entity) = when (entity) {
        is Character -> distanceTo(entity.tile, entity.size.width, entity.size.height)
        is GameObject -> distanceTo(entity.tile, entity.width, entity.height)
        else -> distanceTo(entity.tile)
    }

    fun distanceTo(other: Tile, width: Int, height: Int) = distanceTo(Distance.getNearest(other, width, height, this))

    fun distanceTo(other: Tile): Int {
        if (plane != other.plane) {
            return -1
        }
        return Distance.chebyshev(x, y, other.x, other.y)
    }

    fun within(other: Tile, radius: Int): Boolean {
        return Distance.within(x, y, plane, other.x, other.y, other.plane, radius)
    }

    fun within(x: Int, y: Int, plane: Int, radius: Int): Boolean {
        return Distance.within(this.x, this.y, this.plane, x, y, plane, radius)
    }

    fun toCuboid(width: Int = 1, height: Int = 1) = Cuboid(this, width, height, 1)
    fun toCuboid(radius: Int) = Cuboid(minus(radius, radius), radius * 2 + 1, radius * 2 + 1, 1)

    override fun toString(): String {
        return "Tile($x, $y, $plane)"
    }

    companion object {
        fun id(x: Int, y: Int, plane: Int = 0) = (y and 0x3fff) + ((x and 0x3fff) shl 14) + ((plane and 0x3) shl 28)
        fun x(id: Int) = id shr 14 and 0x3fff
        fun y(id: Int) = id and 0x3fff
        fun plane(id: Int) = id shr 28

        val EMPTY = Tile(0)

        fun fromMap(map: Map<String, Any>) = Tile(map["x"] as Int, map["y"] as Int, map["plane"] as? Int ?: 0)

        /**
         * Index for a tile within a [Chunk]
         * Used for indexing tiles in arrays
         */
        fun index(x: Int, y: Int): Int = (x and 0x7) or ((y and 0x7) shl 3)
        fun index(x: Int, y: Int, group: Int): Int = index(x, y) or ((group and 0x7) shl 6)
        fun indexX(index: Int) = index and 0x7
        fun indexY(index: Int) = index shr 3 and 0x7
        fun indexGroup(index: Int) = index shr 6 and 0x7
    }
}

fun Tile.equals(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = this.x == x && this.y == y && this.plane == plane