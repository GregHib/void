package world.gregs.voidps.engine.map

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import world.gregs.voidps.engine.data.serializer.TileSerializer
import world.gregs.voidps.engine.map.area.Coordinate3D
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionPlane
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.BresenhamsLine
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
@JsonSerialize(using = TileSerializer::class)
inline class Tile(val id: Int) : Coordinate3D {

    constructor(x: Int, y: Int, plane: Int = 0) : this(getId(x, y, plane))

    override val x: Int
        get() = getX(id)
    override val y: Int
        get() = getY(id)
    override val plane: Int
        get() = getPlane(id)

    val chunk: Chunk
        get() = Chunk(x / 8, y / 8, plane)
    val region: Region
        get() = Region(x / 64, y / 64)
    val regionPlane: RegionPlane
        get() = RegionPlane(x / 64, y / 64, plane)

    fun copy(x: Int = this.x, y: Int = this.y, plane: Int = this.plane) = Tile(x, y, plane)
    override fun add(x: Int, y: Int, plane: Int) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)

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

    override fun add(x: Int, y: Int) = add(x, y, 0)

    fun withinSight(other: Tile): Boolean {
        return get<BresenhamsLine>().withinSight(this, other) is PathResult.Success
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