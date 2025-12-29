package world.gregs.voidps.tools.map

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.MAPS
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapTile
import world.gregs.voidps.type.Region

class MapDecoder(val xteas: Map<Int, IntArray>? = null) : DefinitionDecoder<MapDefinition>(MAPS) {

    private val objects = MapObjectDefinitionDecoder(xteas)

    override fun MapDefinition.read(opcode: Int, buffer: Reader) = throw UnsupportedOperationException("Not in use.")

    override fun create(size: Int) = Array(size) { MapDefinition() }

    override fun size(cache: Cache): Int = cache.lastArchiveId(index)

    override fun getArchive(id: Int): Int = id

    override fun getFile(id: Int): Int = 0

    val regionHashes: MutableMap<Int, Int> = Int2IntOpenHashMap(1600)
    var modified = true

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
        val reader = ArrayReader(data)
        val definition = definitions[id]
        definition.id = region
        loadTiles(reader, definition.tiles)
        objects.decode(cache, definition, modified)
    }

    private fun loadTiles(reader: Reader, tiles: LongArray) {
        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    var height = 0
                    var attrOpcode = 0
                    var overlayPath = 0
                    var overlayRotation = 0
                    var overlayId = 0
                    var settings = 0
                    var underlayId = 0
                    loop@ while (true) {
                        val config = reader.readUnsignedByte()
                        if (config == 0) {
                            break@loop
                        } else if (config == 1) {
                            height = reader.readUnsignedByte()
                            break@loop
                        } else if (config <= 49) {
                            attrOpcode = config
                            overlayId = reader.readUnsignedByte()
                            overlayPath = (config - 2) / 4
                            overlayRotation = 3 and (config - 2)
                        } else if (config <= 81) {
                            settings = config - 49
                        } else {
                            underlayId = (config - 81) and 0xff
                        }
                    }
                    if (height != 0 || attrOpcode != 0 || overlayPath != 0 || overlayRotation != 0 || overlayId != 0 || settings != 0 || underlayId != 0) {
                        tiles[MapDefinition.index(localX, localY, level)] = MapTile.pack(
                            height,
                            attrOpcode,
                            overlayId,
                            overlayPath,
                            overlayRotation,
                            settings,
                            underlayId,
                        )
                    }
                }
            }
        }
    }

    override fun readLoop(definition: MapDefinition, buffer: Reader) {
    }
}
