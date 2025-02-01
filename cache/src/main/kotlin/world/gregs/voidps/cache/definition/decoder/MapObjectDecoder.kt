package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.cache.definition.data.MapDefinition

/**
 * Decodes all objects in a map except bridges
 */
abstract class MapObjectDecoder {

    /**
     * Decodes object information and calls [add] for each using [settings] to skip bridge objects
     */
    fun decode(buffer: ByteArray, settings: ByteArray, regionTileX: Int, regionTileY: Int) {
        var position = 0
        var objectId = -1
        while (true) {
            /*
            val skip = readLargeSmart()
            if (skip == 0) {
                break
            }
            */
            // Decomposed for early exit
            var peek = buffer[position++].toInt() and 0xff
            val skip = when {
                peek == 0 -> break
                peek >= 128 -> {
                    var lastValue = (peek shl 8 or (buffer[position++].toInt() and 0xff)) - 32768
                    var baseValue = 0
                    if (lastValue == 32767) {
                        peek = buffer[position++].toInt() and 0xff
                        lastValue = if (peek < 128) {
                            peek
                        } else {
                            (peek shl 8 or (buffer[position++].toInt() and 0xff)) - 32768
                        }
                        baseValue += 32767
                    }
                    baseValue + lastValue
                }
                else -> peek
            }
            objectId += skip
            var tile = 0
            while (true) {
                /*
                val loc = reader.readSmart()
                if (loc == 0) {
                    break
                }
                tile += loc - 1
                */
                // Decomposed for early exit
                val loc = buffer[position++].toInt() and 0xff
                tile += when {
                    loc == 0 -> break
                    loc >= 128 -> (loc shl 8 or (buffer[position++].toInt() and 0xff)) - 32769
                    else -> loc - 1
                }

                var level = tile shr 12

                // Decrease bridges
                if (isBridge(settings, tile)) {
                    if (--level == -1) {
                        position++
                        continue
                    }
                }
                // Data
                val localX = MapDefinition.localX(tile)
                val localY = MapDefinition.localY(tile)
                val data = buffer[position++].toInt()
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

        private fun isBridge(settings: ByteArray, tile: Int): Boolean {
            // Check level 1
            return settings[0x1000 + (tile and 0xfff)].toInt() and BRIDGE_TILE == BRIDGE_TILE
        }
    }
}
