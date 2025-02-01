package world.gregs.voidps.tools.map

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.decoder.MapObjectDecoder

/**
 * Adds all objects except bridges to a [MapDefinition]
 */
class MapObjectDefinitionDecoder(
    val xteas: Map<Int, IntArray>? = null
) : MapObjectDecoder() {

    lateinit var definition: MapDefinition

    fun decode(cache: Cache, definition: MapDefinition) {
        this.definition = definition
        val regionX = definition.id shr 8
        val regionY = definition.id and 0xff
        val objectData = cache.data(Index.MAPS, "l${regionX}_$regionY", xteas?.get(definition.id)) ?: return
        val reader = BufferReader(objectData)
        super.decode(reader, definition.tiles, -1, -1)
    }

    override fun add(objectId: Int, localX: Int, localY: Int, level: Int, shape: Int, rotation: Int, regionTileX: Int, regionTileY: Int) {
        definition.objects.add(MapObject(objectId, localX, localY, level, shape, rotation))
    }
}
