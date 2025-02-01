package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapTile

/**
 * Loads all
 */
object MapTileDecoder {
    fun loadTiles(reader: Reader, tiles: LongArray) {
        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    var height = 0
                    var attrOpcode = 0
                    var overlayPath = 0
                    var overlayRotation = 0
                    var overlayId = 0
                    var settings = 0
                    var underlayId = 0
                    loop@ while (true) {
                        val config = reader.readUnsignedByte()
                        if (config == 0) {
                            break@loop
                        } else if (config == 1) {
                            height = reader.readUnsignedByte()
                            break@loop
                        } else if (config <= 49) {
                            attrOpcode = config
                            overlayId = reader.readUnsignedByte()
                            overlayPath = (config - 2) / 4
                            overlayRotation = 3 and (config - 2)
                        } else if (config <= 81) {
                            settings = config - 49
                        } else {
                            underlayId = (config - 81) and 0xff
                        }
                    }
                    if (height != 0 || attrOpcode != 0 || overlayPath != 0 || overlayRotation != 0 || overlayId != 0 || settings != 0 || underlayId != 0) {
                        tiles[MapDefinition.index(localX, localY, level)] = MapTile.pack(
                            height,
                            attrOpcode,
                            overlayId,
                            overlayPath,
                            overlayRotation,
                            settings,
                            underlayId
                        )
                    }
                }
            }
        }
    }


    private val indices = IntArray(16384)

    init {
        var i = 0
        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    indices[i++] = MapDefinition.index(localX, localY, level)
                }
            }
        }
    }

    fun loadTiles(data: ByteArray, tiles: ByteArray) {
        var position = 0
        val size = data.size
        for (index in indices) {
            var settings = 0
            while (position < size) {
                val config = data[position++].toInt() and 0xff
                when {
                    config == 0 -> {
                        tiles[index] = settings.toByte()
                        break
                    }
                    config == 1 -> {
                        position++
                        tiles[index] = settings.toByte()
                        break
                    }
                    config <= 49 -> position++
                    config <= 81 -> settings = config - 49
                }
            }
        }
    }
}