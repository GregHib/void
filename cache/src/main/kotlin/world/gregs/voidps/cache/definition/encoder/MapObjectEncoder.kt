package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.MapDefinition

/**
 * Encodes [MapDefinition] the trouble with objects is the decoder modifies the level
 * When a bridge exists, so the original data needs to be stored separately.
 * Make sure to use MapObjectDefinitionDecoder#modified = false
 */
class MapObjectEncoder : DefinitionEncoder<MapDefinition> {

    override fun Writer.encode(definition: MapDefinition) {
        if (definition.id == -1) {
            return
        }
        var id = -1
        definition.objects.groupBy { it.id }.toList().sortedBy { it.first }.forEach { (objectId, objects) ->
            val difference = objectId - id
            id += difference
            writeLargeSmart(difference)
            var location = 0
            objects.sortedBy { MapDefinition.index(it.x, it.y, it.level) }.forEach { loc ->
                val local = MapDefinition.index(loc.x, loc.y, loc.level)
                val dif = local - location
                location += dif
                writeSmart(dif + 1)
                writeSmart(loc.rotation or (loc.shape shl 2))
            }
            writeSmart(0)
        }
        writeSmart(0)
    }

    private fun Writer.writeLargeSmart(value: Int) {
        if (value >= 32767) {
            for (i in 0 until value / 32767) {
                writeSmart(32767)
            }
            writeSmart(value.rem(32767))
        } else {
            writeSmart(value)
        }
    }

}