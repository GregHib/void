package world.gregs.voidps.engine.map.instance

import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Coordinate2D
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region

/**
 * @author GregHib <greg@gregs.world>
 * @since July 05, 2020
 */
inline class Instance(val id: Int) : Coordinate2D {

    constructor(x: Int, y: Int) : this(getId(x, y))

    override val x: Int
        get() = getX(id)
    override val y: Int
        get() = getY(id)
    val chunk: Chunk
        get() = Chunk(x * 8, y * 8)
    val region: Region
        get() = Region(x, y)
    val tile: Tile
        get() = Tile(x * 64, y * 64, 0)

    fun copy(x: Int = this.x, y: Int = this.y) = Instance(x, y)
    override fun add(x: Int, y: Int) = copy(x = this.x + x, y = this.y + y)
    fun minus(x: Int = 0, y: Int = 0) = add(-x, -y)
    fun delta(x: Int = 0, y: Int = 0) = Delta(this.x - x, this.y - y)

    fun add(point: Instance) = add(point.x, point.y)
    fun minus(point: Instance) = minus(point.x, point.y)
    fun delta(point: Instance) = delta(point.x, point.y)

    companion object {
        fun createSafe(x: Int, y: Int) = Instance(x.coerceIn(0x5d, 0xff), y and 0xff)
        fun getId(x: Int, y: Int) = (y and 0xff) + ((x and 0xff) shl 8)
        fun getX(id: Int) = id shr 8
        fun getY(id: Int) = id and 0xff
        val EMPTY = Instance(0x5d, 0)
    }
}

fun Instance.equals(x: Int, y: Int) = this.x == x && this.y == y