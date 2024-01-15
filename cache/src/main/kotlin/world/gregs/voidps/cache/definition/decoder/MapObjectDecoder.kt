package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapTile

/**
 * Decodes all objects in a map except bridges
 */
abstract class MapObjectDecoder {

    /**
     * Decodes object information and calls [add] for each using [tiles] to skip bridge objects
     */
    fun decode(reader: BufferReader, tiles: LongArray, regionTileX: Int, regionTileY: Int) {
        var objectId = -1
        while (true) {
            val skip = reader.readLargeSmart()
            if (skip == 0) {
                break
            }
            objectId += skip
            var tile = 0
            while (true) {
                val loc = reader.readSmart()
                if (loc == 0) {
                    break
                }
                tile += loc - 1

                // Data
                val localX = tile shr 6 and 0x3f
                val localY = tile and 0x3f
                var level = tile shr 12
                val data = reader.readUnsignedByte()

                // Decrease bridges
                if (isBridge(tiles, localX, localY)) {
                    level--
                }

                // Validate level
                if (level !in 0 until 4) {
                    continue
                }

                val shape = data shr 2
                val rotation = data and 0x3

                // Valid object
                add(objectId, localX, localY, level, shape, rotation, regionTileX, regionTileY)
            }
        }
    }

    abstract fun add(objectId: Int, localX: Int, localY: Int, level: Int, shape: Int, rotation: Int, regionTileX: Int, regionTileY: Int)

    companion object {
        private const val BRIDGE_TILE = 0x2

        private fun isBridge(tiles: LongArray, localX: Int, localY: Int): Boolean {
            return MapTile.settings(tiles[MapDefinition.index(localX, localY, 1)]) and BRIDGE_TILE == BRIDGE_TILE
        }
    }
}
