package rs.dusk.engine.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
inline class Tile(val id: Int) {

    constructor(x: Int, y: Int, plane: Int) : this((y and 0x3fff) + ((x and 0x3fff) shl 14) + ((plane and 0x3) shl 28))

    val x: Int
        get() = id shr 14 and 0x3fff

    val y: Int
        get() = id and 0x3fff

    val plane: Int
        get() = id shr 28

    val chunk
        get() = Chunk(x / 8, y / 8)

    val region
        get() = Region(x / 64, y / 64)

    companion object {
        fun Tile.add(x: Int = 0, y: Int = 0, plane: Int = 0): Tile {
            return Tile(this.x + x, this.y + y, this.plane + plane)
        }
    }
}