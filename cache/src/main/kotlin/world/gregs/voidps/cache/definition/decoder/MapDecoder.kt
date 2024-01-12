package world.gregs.voidps.cache.definition.decoder

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.MAPS
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.MapTile
import world.gregs.voidps.type.Region

class MapDecoder(val xteas: Map<Int, IntArray>? = null) : DefinitionDecoder<MapDefinition>(MAPS) {

    override fun MapDefinition.read(opcode: Int, buffer: Reader) {
        TODO("Not yet implemented")
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

    override fun loadCache(cache: Cache): Array<MapDefinition> {
        regionHashes.clear()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val archiveId = cache.getArchiveId(index, "m${regionX}_$regionY")
                if (archiveId == -1) {
                    continue
                }
                regionHashes[archiveId] = Region.id(regionX, regionY)
            }
        }
        return super.loadCache(cache)
    }

    override fun load(definitions: Array<MapDefinition>, cache: Cache, id: Int) {
        val region = regionHashes[id] ?: return
        val data = cache.getFile(index, id, 0, null) ?: return
        val reader = BufferReader(data)
        val definition = definitions[id]
        definition.id = region
        readLoop(definition, reader)
        loadObjects(cache, definition)
    }

    override fun readLoop(definition: MapDefinition, buffer: Reader) {
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
                        val config = buffer.readUnsignedByte()
                        if (config == 0) {
                            break@loop
                        } else if (config == 1) {
                            height = buffer.readUnsignedByte()
                            break@loop
                        } else if (config <= 49) {
                            attrOpcode = config
                            overlayId = buffer.readUnsignedByte()
                            overlayPath = (config - 2) / 4
                            overlayRotation = 3 and (config - 2)
                        } else if (config <= 81) {
                            settings = config - 49
                        } else {
                            underlayId = (config - 81) and 0xff
                        }
                    }
                    if (height != 0 || attrOpcode != 0 || overlayPath != 0 || overlayRotation != 0 || overlayId != 0 || settings != 0 || underlayId != 0) {
                        definition.setTile(localX, localY, level, MapTile(
                            height,
                            attrOpcode,
                            overlayId,
                            overlayPath,
                            overlayRotation,
                            settings,
                            underlayId
                        ))
                    }
                }
            }
        }
    }

    private fun loadObjects(cache: Cache, definition: MapDefinition) {
        val objectData = cache.getFile(index, "l${definition.id shr 8}_${definition.id and 0xff}", xteas?.get(definition.id)) ?: return
        val reader = BufferReader(objectData)
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
                val obj = reader.readUnsignedByte()

                // Decrease bridges
                if (definition.getTile(localX, localY, 1).isTile(BRIDGE_TILE)) {
                    level--
                }

                // Validate level
                if (level !in 0 until 4) {
                    continue
                }

                val shape = obj shr 2
                val rotation = obj and 0x3

                // Valid object
                definition.objects.add(MapObject(objectId, localX, localY, level, shape, rotation))
            }
        }
    }

    companion object {
        private const val BRIDGE_TILE = 0x2
    }
}