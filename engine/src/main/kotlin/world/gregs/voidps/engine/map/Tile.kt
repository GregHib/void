package world.gregs.voidps.engine.map

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import world.gregs.voidps.engine.data.serializer.TileDeserializer
import world.gregs.voidps.engine.data.serializer.TileSerializer
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionPlane
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.BresenhamsLine
import world.gregs.voidps.utility.get
import kotlin.math.abs

@JsonSerialize(using = TileSerializer::class)
@JsonDeserialize(using = TileDeserializer::class)
inline class Tile(val id: Int) {

    constructor(x: Int, y: Int, plane: Int = 0) : this(getId(x, y, plane))

    val x: Int
        get() = getX(id)
    val y: Int
        get() = getY(id)
    val plane: Int
        get() = getPlane(id)

    val chunk: Chunk
        get() = Chunk(x / 8, y / 8, plane)
    val region: Region
        get() = Region(x / 64, y / 64)
    val regionPlane: RegionPlane
        get() = RegionPlane(x / 64, y / 64, plane)

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

    fun withinSight(other: Tile): Boolean {
        return get<BresenhamsLine>().withinSight(this, other) is PathResult.Success
    }

    fun distanceTo(entity: Entity) = when (entity) {
        is Character -> distanceTo(entity.tile, entity.size)
        is GameObject -> distanceTo(entity.tile, entity.size)
        else -> distanceTo(entity.tile)
    }

    fun distanceTo(other: Tile, size: Size) = distanceTo(Distance.getNearest(other, size, this))

    fun distanceTo(other: Tile): Int {
        if (plane != other.plane) {
            return -1
        }
        return Distance.chebyshev(x, y, other.x, other.y)
    }

    fun within(other: Tile, radius: Int): Boolean {
        return plane == other.plane && abs(x - other.x) <= radius && abs(y - other.y) <= radius
    }

    fun within(x: Int, y: Int, plane: Int, radius: Int): Boolean {
        return this.plane == plane && abs(this.x - x) <= radius && abs(this.y - y) <= radius
    }

    fun toCuboid(width: Int = 1, height: Int = 1) = Cuboid(this, width - 1, height - 1, 0)

    fun toCuboid(radius: Int) = Cuboid(minus(radius, radius), (radius * 2 + 1) - 1, (radius * 2 + 1) - 1, 0)

    override fun toString(): String {
        return "Tile($x, $y, $plane)"
    }

    companion object {
        fun createSafe(x: Int, y: Int, plane: Int = 0) =
            Tile(x and 0x3fff, y and 0x3fff, plane and 0x3)

        fun getId(x: Int, y: Int, plane: Int = 0) = (y and 0x3fff) + ((x and 0x3fff) shl 14) + ((plane and 0x3) shl 28)

        fun getX(id: Int) = id shr 14 and 0x3fff

        fun getY(id: Int) = id and 0x3fff

        fun getPlane(id: Int) = id shr 28

        val EMPTY = Tile(0)
    }
}

fun Tile.equals(x: Int = 0, y: Int = 0, plane: Int = 0) = this.x == x && this.y == y && this.plane == plane