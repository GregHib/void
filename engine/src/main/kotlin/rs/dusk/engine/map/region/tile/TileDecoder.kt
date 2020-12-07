package rs.dusk.engine.map.region.tile

import rs.dusk.core.io.read.BufferReader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class TileDecoder {

    fun read(data: ByteArray): Array<Array<Array<TileData?>>> {
        val buffer = BufferReader(data)
        val tiles = Array(4) { Array(64) { Array<TileData?>(64) { null } } }
        for (plane in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    val tile = TileData()
                    loop@ while (true) {
                        val config = buffer.readUnsignedByte()
                        when {
                            config == 0 -> break@loop
                            config == 1 -> {
                                tile.height = buffer.readUnsignedByte()
                                break@loop
                            }
                            config <= 49 -> {
                                tile.attrOpcode = config
                                tile.overlayId = buffer.readByte()
                                tile.overlayPath = (config - 2) / 4
                                tile.overlayRotation = 3 and (config - 2)
                            }
                            config <= 81 -> {
                                tile.settings = (config - 49).toByte()
                            }
                            else -> tile.underlayId = config - 81
                        }
                    }

                    if (tile.modified()) {
                        tiles[plane][localX][localY] = tile
                    }
                }
            }
        }
        return tiles
    }
}