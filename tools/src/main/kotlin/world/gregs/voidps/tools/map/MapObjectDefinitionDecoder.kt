package world.gregs.voidps.tools.map

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.MapTile
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
        decode(reader, definition.tiles)
    }

    private fun decode(reader: BufferReader, tiles: LongArray) {
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
                var level = tile shr 12
                val data = reader.readUnsignedByte()

                // Decrease bridges
                if (isBridge(tiles, localX, localY)) {
                    level--
                }

                // Validate level
                if (level !in 0 until 4) {
                    continue
                }

                val shape = data shr 2
                val rotation = data and 0x3

                // Valid object
                add(objectId, localX, localY, level, shape, rotation, -1, -1)
            }
        }
    }

    private fun isBridge(tiles: LongArray, localX: Int, localY: Int): Boolean {
        return MapTile.settings(tiles[MapDefinition.index(localX, localY, 1)]) and 0x2 == 0x2
    }

    override fun add(objectId: Int, localX: Int, localY: Int, level: Int, shape: Int, rotation: Int, regionTileX: Int, regionTileY: Int) {
        definition.objects.add(MapObject(objectId, localX, localY, level, shape, rotation))
    }
}
