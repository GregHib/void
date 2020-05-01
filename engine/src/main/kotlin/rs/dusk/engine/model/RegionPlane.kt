package rs.dusk.engine.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
inline class RegionPlane(val id: Int) {

    constructor(
        regionX: Int,
        regionY: Int,
        plane: Int
    ) : this((regionY and 0xff) + ((regionX and 0xff) shl 8) + ((plane and 0x3) shl 16))

    val x: Int
        get() = id shr 8

    val y: Int
        get() = id and 0xff

    val plane: Int
        get() = id shr 16

    val region
        get() = Region(x, y)

    val chunk
        get() = Chunk(x * 8, y * 8)

    val tile
        get() = Tile(x * 64, y * 64, plane)
}