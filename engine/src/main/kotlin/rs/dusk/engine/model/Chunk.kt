package rs.dusk.engine.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
inline class Chunk(val id: Int) {

    constructor(chunkX: Int, chunkY: Int) : this((chunkY and 0xfff) + ((chunkX and 0xfff) shl 12))

    val x: Int
        get() = id shr 12

    val y: Int
        get() = id and 0xfff

    val region
        get() = Region(x / 8, y / 8)

    val tile
        get() = Tile(x * 8, y * 8, 0)
}