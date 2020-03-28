package org.redrune.engine.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Tile(val value: Int) {// FIXME #46

    constructor(x: Int, y: Int, plane: Int) : this(y + (x shl 14) + (plane shl 28))

    val x: Int
        get() = value shr 14 and 0x3fff

    val y: Int
        get() = value and 0x3fff

    val plane: Int
        get() = value shr 28

    companion object {
        fun Tile.add(x: Int = 0, y: Int = 0, plane: Int = 0): Tile {
            return Tile(this.x + x, this.y + y, this.plane + plane)
        }
    }
}