package rs.dusk.engine.map.region.obj

import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class GameObjectMapWriter {

    /**
     * Writes object locations to ByteArray
     * Note: All [objects] lists must be sorted by [GameObject.tile] using [localId]
     */
    fun write(objects: SortedMap<Int, List<GameObject>>): ByteArray {
        val writer = BufferWriter()
        var id = -1
        objects.forEach { (objectId, objects) ->
            val difference = objectId - id
            id += difference
            writer.writeLargeSmart(difference)
            var location = 0
            objects.forEach { loc ->
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
            repeat(value / 32767) {
                writeSmart(32767)
            }
            writeSmart(value.rem(32767))
        } else {
            writeSmart(value)
        }
    }

    companion object {
        fun localId(tile: Tile) = tile.y or (tile.x shl 6) or (tile.plane shl 12)
    }

}