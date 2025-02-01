package world.gregs.voidps.tools.map

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.MAPS
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.decoder.MapTileDecoder
import world.gregs.voidps.type.Region

class MapDecoder(val xteas: Map<Int, IntArray>? = null) : DefinitionDecoder<MapDefinition>(MAPS) {

    private val objects = MapObjectDefinitionDecoder(xteas)

    override fun MapDefinition.read(opcode: Int, buffer: Reader) {
        throw UnsupportedOperationException("Not in use.")
    }

    override fun create(size: Int) = Array(size) { MapDefinition(it) }

    override fun size(cache: Cache): Int {
        return cache.lastArchiveId(index)
    }

    override fun getArchive(id: Int): Int {
        return id
    }

    override fun getFile(id: Int): Int {
        return 0
    }

    val regionHashes: MutableMap<Int, Int> = Int2IntOpenHashMap(1600)

    override fun load(cache: Cache): Array<MapDefinition> {
        regionHashes.clear()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val archiveId = cache.archiveId(index, "m${regionX}_$regionY")
                if (archiveId == -1) {
                    continue
                }
                regionHashes[archiveId] = Region.id(regionX, regionY)
            }
        }
        return super.load(cache)
    }

    override fun load(definitions: Array<MapDefinition>, cache: Cache, id: Int) {
        val region = regionHashes[id] ?: return
        val data = cache.data(index, id, 0, null) ?: return
        val reader = BufferReader(data)
        val definition = definitions[id]
        definition.id = region
        MapTileDecoder.loadTiles(reader, definition.tiles)
        objects.decode(cache, definition)
    }

    override fun readLoop(definition: MapDefinition, buffer: Reader) {
    }
}