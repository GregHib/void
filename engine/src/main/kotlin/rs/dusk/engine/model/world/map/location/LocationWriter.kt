package rs.dusk.engine.model.world.map.location

import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.world.Tile
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class LocationWriter {

    /**
     * Writes locations to ByteArray
     * Note: All [locations] lists must be sorted by [Location.tile] using [localId]
     */
    fun write(locations: SortedMap<Int, List<Location>>): ByteArray {
        val writer = BufferWriter()
        var id = -1
        locations.forEach { (objectId, locations) ->
            val difference = objectId - id
            id += difference
            writer.writeLargeSmart(difference)
            var location = 0
            locations.forEach { loc ->
                val local = localId(loc.tile)
                val dif = local - location
                location += dif
                writer.writeSmart(dif + 1)
                writer.writeSmart(loc.rotation or (loc.type shl 2))
            }
            writer.writeSmart(0)
        }
        writer.writeSmart(0)
        return writer.toArray()
    }

    private fun Writer.writeLargeSmart(value: Int) {
        if (value >= 32767) {
            writeSmart(32767)
            writeSmart(value - 32767)
        } else {
            writeSmart(value)
        }
    }

    companion object {
        fun localId(tile: Tile) = tile.y or (tile.x shl 6) or (tile.plane shl 12)
    }

}