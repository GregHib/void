package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapTile

/**
 * Loads all
 */
object MapTileDecoder {

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