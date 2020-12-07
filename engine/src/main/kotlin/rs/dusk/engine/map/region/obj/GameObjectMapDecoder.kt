package rs.dusk.engine.map.region.obj

import org.koin.dsl.module
import rs.dusk.core.io.read.BufferReader
import rs.dusk.engine.map.region.tile.BRIDGE_TILE
import rs.dusk.engine.map.region.tile.TileData

val objectMapDecoderModule = module {
    single { GameObjectMapDecoder() }
}

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class GameObjectMapDecoder {

    fun read(data: ByteArray, settings: Array<Array<Array<TileData?>>>): List<GameObjectLoc>? {
        var objects: MutableList<GameObjectLoc>? = null
        val reader = BufferReader(data)
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
                var plane = tile shr 12
                val obj = reader.readUnsignedByte()
                val type = obj shr 2
                val rotation = obj and 0x3

                // Validate region
                if (localX < 0 || localX > 64 || localY < 0 || localY >= 64) {
                    continue
                }

                // Decrease bridges
                if (settings[1][localX][localY]?.isTile(BRIDGE_TILE) == true) {
                    plane--
                }

                // Validate plane
                if (plane !in 0 until 4) {
                    continue
                }

                if(objects == null) {
                    objects = mutableListOf()
                }
                // Valid object
                objects.add(GameObjectLoc(objectId, localX, localY, plane, type, rotation))
            }
        }
        return objects
    }
}