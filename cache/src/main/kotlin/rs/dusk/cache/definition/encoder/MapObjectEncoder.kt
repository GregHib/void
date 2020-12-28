package rs.dusk.cache.definition.encoder

import rs.dusk.buffer.write.Writer
import rs.dusk.cache.DefinitionEncoder
import rs.dusk.cache.definition.data.MapDefinition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since December 28, 2020
 */
class MapObjectEncoder : DefinitionEncoder<MapDefinition> {

    override fun Writer.encode(definition: MapDefinition) {
        if (definition.id == -1) {
            return
        }
        var id = -1
        definition.objects.groupBy { it.id }.toSortedMap().forEach { (objectId, objects) ->
            val difference = objectId - id
            id += difference
            writeLargeSmart(difference)
            var location = 0
            objects.forEach { loc ->
                val local = MapDefinition.getHash(loc.x, loc.y, loc.plane)
                val dif = local - location
                location += dif
                writeSmart(dif + 1)
                writeSmart(loc.rotation or (loc.type shl 2))
            }
            writeSmart(0)
        }
        writeSmart(0)
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

}