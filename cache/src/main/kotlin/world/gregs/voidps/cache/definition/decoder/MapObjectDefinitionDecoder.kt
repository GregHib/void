package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.type.Region

/**
 * Adds all objects except bridges to a [MapDefinition]
 */
class MapObjectDefinitionDecoder(
    val definitions: Array<MapDefinition>,
    val xteas: Map<Int, IntArray>? = null
) : MapObjectDecoder() {

    fun loadObjects(cache: Cache, definition: MapDefinition) {
        val regionX = definition.id shr 8
        val regionY = definition.id and 0xff
        val objectData = cache.getFile(Index.MAPS, "l${regionX}_$regionY", xteas?.get(definition.id)) ?: return
        val reader = BufferReader(objectData)
        super.loadObjects(reader, definition.tiles, regionX, regionY)
    }

    override fun add(objectId: Int, localX: Int, localY: Int, level: Int, shape: Int, rotation: Int, regionX: Int, regionY: Int) {
        definitions[Region.id(regionX, regionY)].objects.add(MapObject(objectId, localX, localY, level, shape, rotation))
    }
}
