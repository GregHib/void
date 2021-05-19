package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.area.Coordinate2D
import kotlin.random.Random

inline class Region(val id: Int) : Coordinate2D, Area {

    constructor(x: Int, y: Int) : this(getId(x, y))

    override val x: Int
        get() = getX(id)

    override val y: Int
        get() = getY(id)

    val tile: Tile
        get() = Tile(x * 64, y * 64, 0)

    fun copy(x: Int = this.x, y: Int = this.y) = Region(x, y)
    override fun add(x: Int, y: Int) = copy(x = this.x + x, y = this.y + y)
    fun minus(x: Int = 0, y: Int = 0) = add(-x, -y)
    fun delta(x: Int = 0, y: Int = 0) = Delta(this.x - x, this.y - y)

    fun add(point: Region) = add(point.x, point.y)
    fun minus(point: Region) = minus(point.x, point.y)
    fun delta(point: Region) = delta(point.x, point.y)

    fun toPlane(plane: Int) = RegionPlane(x, y, plane)

    override val area: Double
        get() = 4096.0

    override val regions: Set<Region>
        get() = setOf(this)

    override fun contains(tile: Tile): Boolean = tile.region == this

    override fun random(): Tile {
        return tile.add(Random.nextInt(0, 64), Random.nextInt(0, 64))
    }

    companion object {
        fun createSafe(x: Int, y: Int) = Region(x and 0xff, y and 0xff)
        fun getId(x: Int, y: Int) = (y and 0xff) + ((x and 0xff) shl 8)
        fun getX(id: Int) = id shr 8
        fun getY(id: Int) = id and 0xff
        val EMPTY = Region(0, 0)
    }
}

fun Region.equals(x: Int, y: Int) = this.x == x && this.y == y