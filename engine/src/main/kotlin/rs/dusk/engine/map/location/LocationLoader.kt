package rs.dusk.engine.map.location

import rs.dusk.core.io.read.BufferReader
import rs.dusk.engine.model.Tile
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class LocationLoader {

    val locations: Locations by inject()

    fun load(data: ByteArray, overlays: Array<Array<IntArray>>?) {
        val stream = BufferReader(data)
        var objectId = -1
        while (true) {
            val skip = stream.readLargeSmart()
            if (skip == 0) {
                break
            }
            objectId += skip
            var location = 0
            while (true) {
                val loc = stream.readSmart()
                if (loc == 0) {
                    break
                }
                location += loc - 1

                // Data
                val localX = location shr 6 and 0x3f
                val localY = location and 0x3f
                var plane = location shr 12
                val obj = stream.readUnsignedByte()
                val type = obj shr 2
                val rotation = obj and 0x3

                // Validate region
                if (localX < 0 || localX > 64 || localY < 0 || localY >= 64) {
                    continue
                }

                // Decrease bridges
                if (overlays != null && overlays[1][localX][localY] and BRIDGE_TILE == BRIDGE_TILE) {
                    plane--
                }

                // Validate plane
                if (plane !in 0 until 4) {
                    continue
                }

                // Valid object
                locations.put(Tile(localX, localY, plane), Location(objectId, type, rotation))
            }
        }
    }

    companion object {
        const val BRIDGE_TILE = 0x2
    }
}